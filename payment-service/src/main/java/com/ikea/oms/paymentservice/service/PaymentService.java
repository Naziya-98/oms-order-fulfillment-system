package com.ikea.oms.paymentservice.service;

import com.ikea.oms.paymentservice.dto.PaymentResponseDTO;
import com.ikea.oms.paymentservice.entity.Payment;
import com.ikea.oms.paymentservice.entity.PaymentStatus;
import com.ikea.oms.paymentservice.event.OrderCreatedEvent;
import com.ikea.oms.paymentservice.event.PaymentCompletedEvent;
import com.ikea.oms.paymentservice.event.PaymentFailedEvent;
import com.ikea.oms.paymentservice.exception.PaymentNotFoundException;
import com.ikea.oms.paymentservice.kafka.PaymentProducer;
import com.ikea.oms.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final PaymentGatewaySimulator gatewaySimulator;

    /**
     * Entry point called from the Kafka consumer when order-created-topic
     * fires. Persists a PENDING payment record, then calls out to the
     * simulated gateway. The gateway responds asynchronously (after its
     * configured delay), at which point we update the record and publish
     * either payment-completed-topic or payment-failed-topic.
     */
    @Transactional
    public void processPayment(OrderCreatedEvent event) {

        Payment payment = paymentRepository.findByOrderNumber(event.getOrderNumber())
                .orElseGet(Payment::new);

        // Idempotency guard: if we've already resolved this order, don't reprocess
        // (protects against Kafka re-delivery / consumer restarts).
        if (payment.getStatus() != null && payment.getStatus() != PaymentStatus.PENDING) {
            log.warn(
                    "Payment for OrderNumber={} already resolved with status={}. Skipping reprocessing.",
                    event.getOrderNumber(),
                    payment.getStatus()
            );
            return;
        }

        payment.setOrderId(event.getOrderId());
        payment.setOrderNumber(event.getOrderNumber());
        payment.setSkuCode(event.getSkuCode());
        payment.setQuantity(event.getQuantity());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayTransactionId(UUID.randomUUID().toString());

        paymentRepository.save(payment);

        log.info(
                "Payment record created with PENDING status for OrderNumber={}. Calling payment gateway...",
                event.getOrderNumber()
        );

        gatewaySimulator.authorizeAsync(event.getOrderNumber(), approved -> {
            if (approved) {
                markSuccess(event);
            } else {
                markFailed(event, "DECLINED_BY_GATEWAY");
            }
        });
    }

    @Transactional
    public void markSuccess(OrderCreatedEvent event) {

        paymentRepository.findByOrderNumber(event.getOrderNumber()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        });

        log.info("Payment completed successfully for OrderNumber={}", event.getOrderNumber());

        paymentProducer.publishPaymentCompletedEvent(
                new PaymentCompletedEvent(event.getOrderId(), event.getOrderNumber(), "PAYMENT_COMPLETED")
        );
    }

    @Transactional
    public void markFailed(OrderCreatedEvent event, String reason) {

        paymentRepository.findByOrderNumber(event.getOrderNumber()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });

        log.error("Payment failed for OrderNumber={}, reason={}", event.getOrderNumber(), reason);

        paymentProducer.publishPaymentFailedEvent(
                new PaymentFailedEvent(
                        event.getOrderId(),
                        event.getOrderNumber(),
                        event.getSkuCode(),
                        event.getQuantity(),
                        "PAYMENT_FAILED"
                )
        );
    }

    /**
     * Manual compensating action - lets a caller (Postman, orchestrator,
     * an ops dashboard, etc.) explicitly cancel/reverse a payment instead
     * of relying on magic input data to trigger the failure path. This is
     * the "cancel payment API" Ashutosh asked for.
     */
    @Transactional
    public PaymentResponseDTO cancelPayment(String orderNumber) {

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for OrderNumber: " + orderNumber));

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            log.warn("Payment for OrderNumber={} is already cancelled. Ignoring duplicate request.", orderNumber);
            return toDto(payment);
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment saved = paymentRepository.save(payment);

        log.info("Payment manually cancelled (compensating action) for OrderNumber={}", orderNumber);

        paymentProducer.publishPaymentFailedEvent(
                new PaymentFailedEvent(
                        payment.getOrderId(),
                        payment.getOrderNumber(),
                        payment.getSkuCode(),
                        payment.getQuantity(),
                        "PAYMENT_CANCELLED"
                )
        );

        return toDto(saved);
    }

    // ------------------------------------------------------------------
    // Orchestrated-flow persistence (saga.* commands via
    // SagaPaymentCommandConsumer / SagaCancelPaymentCommandConsumer).
    // Mirrors the choreography methods above, but is keyed by the saga's
    // own SAGA... orderNumber instead of the ORD... business orderNumber:
    // ProcessPaymentCommand is sent by the orchestrator in parallel with
    // CreateOrderCommand, so the ORD... number is not guaranteed to exist
    // yet at the moment payment processing starts here. The SAGA... number
    // is generated up front and never changes, so it's always safe to key on.
    // ------------------------------------------------------------------

    @Transactional
    public void createPendingOrchestratedPayment(Long orderId, String sagaOrderNumber, String skuCode, Integer quantity) {

        Payment payment = paymentRepository.findByOrderNumber(sagaOrderNumber).orElseGet(Payment::new);

        if (payment.getStatus() != null && payment.getStatus() != PaymentStatus.PENDING) {
            log.warn(
                    "Orchestrated payment for OrderNumber={} already resolved with status={}. Skipping reprocessing.",
                    sagaOrderNumber,
                    payment.getStatus()
            );
            return;
        }

        payment.setOrderId(orderId);
        payment.setOrderNumber(sagaOrderNumber);
        payment.setSkuCode(skuCode);
        payment.setQuantity(quantity);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setGatewayTransactionId(UUID.randomUUID().toString());

        paymentRepository.save(payment);

        log.info("Orchestrated payment record created with PENDING status for OrderNumber={}", sagaOrderNumber);
    }

    @Transactional
    public void markOrchestratedSuccess(String sagaOrderNumber) {
        paymentRepository.findByOrderNumber(sagaOrderNumber).ifPresentOrElse(payment -> {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        }, () -> log.warn("No orchestrated payment row found for OrderNumber={} to mark SUCCESS", sagaOrderNumber));
    }

    @Transactional
    public void markOrchestratedFailed(String sagaOrderNumber) {
        paymentRepository.findByOrderNumber(sagaOrderNumber).ifPresentOrElse(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }, () -> log.warn("No orchestrated payment row found for OrderNumber={} to mark FAILED", sagaOrderNumber));
    }

    @Transactional
    public void cancelOrchestratedPayment(String sagaOrderNumber) {
        paymentRepository.findByOrderNumber(sagaOrderNumber).ifPresentOrElse(payment -> {
            if (payment.getStatus() == PaymentStatus.CANCELLED) {
                log.warn("Orchestrated payment for OrderNumber={} is already cancelled. Ignoring duplicate request.", sagaOrderNumber);
                return;
            }
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            log.info("Orchestrated payment cancelled for OrderNumber={}", sagaOrderNumber);
        }, () -> log.warn(
                "No orchestrated payment row found for OrderNumber={} to cancel " +
                "(cancel likely arrived before the PENDING row was created — race is acceptable here " +
                "since PAYMENT_FAILED/PAYMENT_CANCELLED is only reachable from PAYMENT_COMPLETED in the saga).",
                sagaOrderNumber));
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toDto).toList();
    }

    public PaymentResponseDTO getPaymentByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumber(orderNumber)
                .map(this::toDto)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for OrderNumber: " + orderNumber));
    }

    private PaymentResponseDTO toDto(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setOrderNumber(payment.getOrderNumber());
        dto.setSkuCode(payment.getSkuCode());
        dto.setQuantity(payment.getQuantity());
        dto.setStatus(payment.getStatus().name());
        dto.setGatewayTransactionId(payment.getGatewayTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}
