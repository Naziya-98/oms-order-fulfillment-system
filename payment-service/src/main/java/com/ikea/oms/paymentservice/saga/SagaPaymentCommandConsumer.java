package com.ikea.oms.paymentservice.saga;

import com.ikea.oms.paymentservice.service.PaymentGatewaySimulator;
import com.ikea.oms.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaPaymentCommandConsumer {

    private final SagaPaymentEventProducer sagaPaymentEventProducer;
    private final PaymentGatewaySimulator gatewaySimulator;
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "saga.process-payment.command",
            groupId = "payment-orchestration-group",
            containerFactory = "sagaProcessPaymentKafkaListenerFactory"
    )
    public void consume(ProcessPaymentCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Payment Service (Orchestrated) ==========");
        log.info("ProcessPaymentCommand received for OrderNumber={}", command.getOrderNumber());

        // Persist a PENDING row up front, same as the choreography flow — this is
        // what makes paymentdb.payment actually have a row for orchestrated orders.
        // Keyed by the saga's own orderNumber (see PaymentService for why).
        paymentService.createPendingOrchestratedPayment(
                command.getOrderId(), command.getOrderNumber(), command.getSkuCode(), command.getQuantity()
        );

        // Outcome now comes from the simulated payment gateway (timer +
        // configurable failure rate) instead of inspecting skuCode.
        gatewaySimulator.authorizeAsync(command.getOrderNumber(), approved -> {

            if (approved) {

                log.info("[SAGA] Payment completed successfully for OrderNumber={}", command.getOrderNumber());

                paymentService.markOrchestratedSuccess(command.getOrderNumber());

                SagaPaymentCompletedEvent completedEvent = new SagaPaymentCompletedEvent(
                        command.getOrderId(),
                        command.getOrderNumber(),
                        "PAYMENT_COMPLETED"
                );

                sagaPaymentEventProducer.publishPaymentCompleted(completedEvent);

            } else {

                log.error("[SAGA] Payment declined by gateway for OrderNumber={}", command.getOrderNumber());

                paymentService.markOrchestratedFailed(command.getOrderNumber());

                SagaPaymentFailedEvent failedEvent = new SagaPaymentFailedEvent(
                        command.getOrderId(),
                        command.getOrderNumber(),
                        command.getSkuCode(),
                        command.getQuantity(),
                        "PAYMENT_FAILED"
                );

                sagaPaymentEventProducer.publishPaymentFailed(failedEvent);
            }

            log.info("=============================================================");

            // Acknowledge only after the simulated gateway has responded, so a
            // consumer restart mid-flight causes a clean redelivery instead of
            // an orphaned command.
            ack.acknowledge();
        });
    }
}
