package com.ikea.oms.orchestratorservice.service;
import com.ikea.oms.orchestratorservice.command.CancelPaymentCommand;
import com.ikea.oms.orchestratorservice.command.CreateOrderCommand;
import com.ikea.oms.orchestratorservice.command.ProcessPaymentCommand;
import com.ikea.oms.orchestratorservice.command.ReleaseInventoryCommand;
import com.ikea.oms.orchestratorservice.command.ReserveInventoryCommand;
import com.ikea.oms.orchestratorservice.command.SendNotificationCommand;
import com.ikea.oms.orchestratorservice.dto.OrderRequestDTO;
import com.ikea.oms.orchestratorservice.dto.OrderResponseDTO;
import com.ikea.oms.orchestratorservice.entity.SagaOrder;
import com.ikea.oms.orchestratorservice.entity.SagaStatus;
import com.ikea.oms.orchestratorservice.event.InventoryReleasedEvent;
import com.ikea.oms.orchestratorservice.event.SagaInventoryReservedEvent;
import com.ikea.oms.orchestratorservice.event.NotificationSentEvent;
import com.ikea.oms.orchestratorservice.event.OrderDeliveredEvent;
import com.ikea.oms.orchestratorservice.event.PaymentCompletedEvent;
import com.ikea.oms.orchestratorservice.event.PaymentFailedEvent;
import com.ikea.oms.orchestratorservice.exception.SagaOrderNotFoundException;
import com.ikea.oms.orchestratorservice.kafka.SagaCommandProducer;
import com.ikea.oms.orchestratorservice.repository.SagaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * SAGA ORCHESTRATION (central coordinator)
 * ------------------------------------------------------------------
 * Unlike the choreography flow (order/inventory/payment/notification
 * services autonomously reacting to each other's domain events), this
 * class is the single source of truth for "what happens next".
 *
 * It:
 *   1) Commands inventory-service via Kafka to reserve inventory
 *   2) Receives InventoryReservedEvent with unitPrice and success/failure
 *   3) Explicitly commands order-service to persist the order record
 *   4) Explicitly commands payment-service to process payment
 *   5) On success, explicitly commands notification-service to notify + deliver
 *   6) On failure, explicitly commands inventory-service to compensate (release stock)
 *
 * All state transitions are persisted in saga_orders so progress can be
 * inspected via GET /api/orchestrated-orders/{orderNumber}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorService {

    private final SagaOrderRepository sagaOrderRepository;
    private final SagaCommandProducer sagaCommandProducer;

    public OrderResponseDTO startSaga(OrderRequestDTO request) {

        log.info("[ORCHESTRATOR] Saga starting. SKU={}, Quantity={}", request.getSkuCode(), request.getQuantity());

        SagaOrder sagaOrder = new SagaOrder();

        sagaOrder.setOrderNumber("SAGA" + System.currentTimeMillis());
        sagaOrder.setSkuCode(request.getSkuCode());
        sagaOrder.setQuantity(request.getQuantity());
        sagaOrder.setCustomerName(request.getCustomerName());
        sagaOrder.setCustomerEmail(request.getCustomerEmail());
        sagaOrder.setShippingAddress(request.getShippingAddress());
        sagaOrder.setStatus(SagaStatus.STARTED);
        sagaOrder.setCurrentStep("SAGA_STARTED");

        // ---- Step 1: command inventory-service to reserve inventory (async via Kafka) ----

        SagaOrder savedOrder = sagaOrderRepository.save(sagaOrder);

        log.info("[ORCHESTRATOR] SagaOrder persisted. Id={}, OrderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());

        ReserveInventoryCommand reserveCommand = new ReserveInventoryCommand(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getSkuCode(),
                savedOrder.getQuantity()
        );

        sagaCommandProducer.sendReserveInventoryCommand(reserveCommand);

        // Some DB schemas may have a check constraint that doesn't include INVENTORY_RESERVATION_PENDING.
        // To remain compatible with existing DB constraints use STARTED until inventory reservation event arrives.
        savedOrder.setStatus(SagaStatus.STARTED);
        savedOrder.setCurrentStep("RESERVE_INVENTORY_COMMAND_SENT");
        sagaOrderRepository.save(savedOrder);

        return toResponse(savedOrder);
    }

    // ---- Step 2: inventory reserved -> command order-service and payment-service ----
    public void onInventoryReserved(SagaInventoryReservedEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        if ("INVENTORY_RESERVED".equals(event.getStatus())) {

            sagaOrder.setUnitPrice(event.getUnitPrice());
            sagaOrder.setStatus(SagaStatus.INVENTORY_RESERVED);
            sagaOrder.setCurrentStep("INVENTORY_RESERVED");
            sagaOrderRepository.save(sagaOrder);

            log.info("[ORCHESTRATOR] Inventory reserved successfully. OrderNumber={}, UnitPrice={}", 
                     event.getOrderNumber(), event.getUnitPrice());

            // ---- Step 2a: command order-service to persist the order record ----
            CreateOrderCommand createOrderCommand = new CreateOrderCommand(
                    sagaOrder.getId(),
                    null, // Let Order Service generate its own business orderNumber
                    sagaOrder.getSkuCode(),
                    sagaOrder.getQuantity(),
                    sagaOrder.getUnitPrice(),
                    sagaOrder.getCustomerName(),
                    sagaOrder.getCustomerEmail(),
                    sagaOrder.getShippingAddress()
            );

            sagaCommandProducer.sendCreateOrderCommand(createOrderCommand);

            // ---- Step 2b: command payment-service ----
            ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(
                    sagaOrder.getId(),
                    sagaOrder.getOrderNumber(),
                    sagaOrder.getSkuCode(),
                    sagaOrder.getQuantity()
            );

            sagaCommandProducer.sendProcessPaymentCommand(paymentCommand);

            sagaOrder.setStatus(SagaStatus.PAYMENT_PROCESSING);
            sagaOrder.setCurrentStep("PROCESS_PAYMENT_COMMAND_SENT");
            sagaOrderRepository.save(sagaOrder);

        } else {

            sagaOrder.setStatus(SagaStatus.INVENTORY_RESERVATION_FAILED);
            sagaOrder.setFailureReason("Inventory reservation failed for SKU=" + event.getSkuCode());
            sagaOrder.setCurrentStep("INVENTORY_RESERVATION_FAILED");
            sagaOrderRepository.save(sagaOrder);

            log.error("[ORCHESTRATOR] Inventory reservation failed. OrderNumber={}, Reason: {}", 
                      event.getOrderNumber(), event.getStatus());
        }
    }

    public void onOrderCreated(com.ikea.oms.orchestratorservice.event.OrderCreatedEvent event) {

        // Correlate saga with business orderNumber produced by Order Service
        SagaOrder sagaOrder = getByOrderId(event.getSagaOrderId());

        // Do not overwrite saga orderNumber (SAGA...). Store business order number separately.
        sagaOrder.setBusinessOrderNumber(event.getOrderNumber());
        sagaOrder.setCurrentStep("ORDER_CREATED_BY_ORDER_SERVICE");
        sagaOrderRepository.save(sagaOrder);

        log.info("[ORCHESTRATOR] Linked sagaOrderId={} to businessOrderNumber={}", event.getSagaOrderId(), event.getOrderNumber());
    }

    // ---- Step 3a: payment succeeded -> command notification-service ----
    public void onPaymentCompleted(PaymentCompletedEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        sagaOrder.setStatus(SagaStatus.PAYMENT_COMPLETED);
        sagaOrder.setCurrentStep("PAYMENT_COMPLETED");
        sagaOrderRepository.save(sagaOrder);

        SendNotificationCommand notificationCommand =
                new SendNotificationCommand(sagaOrder.getId(), sagaOrder.getOrderNumber());

        sagaCommandProducer.sendNotificationCommand(notificationCommand);

        sagaOrder.setStatus(SagaStatus.NOTIFICATION_PROCESSING);
        sagaOrder.setCurrentStep("SEND_NOTIFICATION_COMMAND_SENT");
        sagaOrderRepository.save(sagaOrder);
    }

    // ---- Step 3b: payment failed -> COMPENSATE by commanding inventory-service to release stock ----
    public void onPaymentFailed(PaymentFailedEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        sagaOrder.setStatus(SagaStatus.PAYMENT_FAILED);
        sagaOrder.setFailureReason("Payment failed for SKU=" + event.getSkuCode());
        sagaOrder.setCurrentStep("PAYMENT_FAILED");
        sagaOrderRepository.save(sagaOrder);

        ReleaseInventoryCommand releaseCommand = new ReleaseInventoryCommand(
                sagaOrder.getId(),
                sagaOrder.getOrderNumber(),
                sagaOrder.getBusinessOrderNumber(),
                sagaOrder.getSkuCode(),
                sagaOrder.getQuantity()
        );

        sagaCommandProducer.sendReleaseInventoryCommand(releaseCommand);

        sagaOrder.setStatus(SagaStatus.COMPENSATING);
        sagaOrder.setCurrentStep("RELEASE_INVENTORY_COMMAND_SENT");
        sagaOrderRepository.save(sagaOrder);
    }

    // ---- Explicit "Cancel Order" API (replaces the FAIL_PAYMENT sku hack) ----
    // POST /api/orchestrated-orders/{orderNumber}/cancel
    // Branches on how far the saga has already progressed:
    //  - payment not completed yet -> release inventory directly
    //  - payment already completed -> ask payment-service to refund first;
    //    the refund confirmation comes back on the EXISTING saga.payment-failed.event
    //    topic, so onPaymentFailed() above completes the compensation as-is.
    private static final Set<SagaStatus> CANCELLABLE_BEFORE_PAYMENT = Set.of(
            SagaStatus.INVENTORY_RESERVED,
            SagaStatus.PAYMENT_PROCESSING
    );

    private static final Set<SagaStatus> CANCELLABLE_AFTER_PAYMENT = Set.of(
            SagaStatus.PAYMENT_COMPLETED,
            SagaStatus.NOTIFICATION_PROCESSING,
            SagaStatus.NOTIFICATION_SENT,
            SagaStatus.COMPLETED
    );

    public OrderResponseDTO cancelOrder(String orderNumber) {

        SagaOrder sagaOrder = sagaOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new SagaOrderNotFoundException("Saga order not found: " + orderNumber));

        SagaStatus currentStatus = sagaOrder.getStatus();

        if (CANCELLABLE_AFTER_PAYMENT.contains(currentStatus)) {

            log.info("[ORCHESTRATOR] Cancel requested after payment completed. Refunding first. OrderNumber={}", orderNumber);

            sagaOrder.setStatus(SagaStatus.REFUND_PROCESSING);
            sagaOrder.setCurrentStep("CANCEL_PAYMENT_COMMAND_SENT");
            sagaOrder.setFailureReason("Order cancelled by customer/admin after payment");
            sagaOrderRepository.save(sagaOrder);

            sagaCommandProducer.sendCancelPaymentCommand(new CancelPaymentCommand(
                    sagaOrder.getId(), sagaOrder.getOrderNumber(), sagaOrder.getSkuCode(), sagaOrder.getQuantity()
            ));

        } else if (CANCELLABLE_BEFORE_PAYMENT.contains(currentStatus)) {

            log.info("[ORCHESTRATOR] Cancel requested before payment completed. Releasing inventory directly. OrderNumber={}", orderNumber);

            sagaOrder.setStatus(SagaStatus.COMPENSATING);
            sagaOrder.setCurrentStep("RELEASE_INVENTORY_COMMAND_SENT");
            sagaOrder.setFailureReason("Order cancelled by customer/admin before payment completed");
            sagaOrderRepository.save(sagaOrder);

            sagaCommandProducer.sendReleaseInventoryCommand(new ReleaseInventoryCommand(
                    sagaOrder.getId(), sagaOrder.getOrderNumber(), sagaOrder.getBusinessOrderNumber(),
                    sagaOrder.getSkuCode(), sagaOrder.getQuantity()
            ));

        } else {
            throw new IllegalStateException("Order cannot be cancelled from its current status: " + currentStatus);
        }

        return toResponse(sagaOrder);
    }

    public void onNotificationSent(NotificationSentEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        sagaOrder.setStatus(SagaStatus.NOTIFICATION_SENT);
        sagaOrder.setCurrentStep("NOTIFICATION_SENT");
        sagaOrderRepository.save(sagaOrder);
    }

    public void onOrderDelivered(OrderDeliveredEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        sagaOrder.setStatus(SagaStatus.COMPLETED);
        sagaOrder.setCurrentStep("ORDER_DELIVERED");
        sagaOrderRepository.save(sagaOrder);

        log.info("[ORCHESTRATOR] Saga COMPLETED successfully. OrderNumber={}", sagaOrder.getOrderNumber());
    }

    // ---- Compensation completed ----
    public void onInventoryReleased(InventoryReleasedEvent event) {

        SagaOrder sagaOrder = getByOrderId(event.getOrderId());

        sagaOrder.setStatus(SagaStatus.CANCELLED);
        sagaOrder.setCurrentStep("INVENTORY_RELEASED");
        sagaOrderRepository.save(sagaOrder);

        log.info("[ORCHESTRATOR] Saga CANCELLED (compensated) successfully. OrderNumber={}", sagaOrder.getOrderNumber());
    }

    public OrderResponseDTO getByOrderNumber(String orderNumber) {

        SagaOrder sagaOrder = sagaOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new SagaOrderNotFoundException("Saga order not found: " + orderNumber));

        return toResponse(sagaOrder);
    }

    public List<OrderResponseDTO> getAll() {

        return sagaOrderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SagaOrder getByOrderId(Long orderId) {
        return sagaOrderRepository.findById(orderId)
                .orElseThrow(() -> new SagaOrderNotFoundException("Saga order not found. Id=" + orderId));
    }

    private OrderResponseDTO toResponse(SagaOrder sagaOrder) {

        OrderResponseDTO response = new OrderResponseDTO();

        response.setId(sagaOrder.getId());
        response.setOrderNumber(sagaOrder.getOrderNumber());
        response.setBusinessOrderNumber(sagaOrder.getBusinessOrderNumber());
        response.setSkuCode(sagaOrder.getSkuCode());
        response.setQuantity(sagaOrder.getQuantity());
        response.setUnitPrice(sagaOrder.getUnitPrice());
        response.setCustomerName(sagaOrder.getCustomerName());
        response.setCustomerEmail(sagaOrder.getCustomerEmail());
        response.setShippingAddress(sagaOrder.getShippingAddress());
        response.setStatus(sagaOrder.getStatus());
        response.setCurrentStep(sagaOrder.getCurrentStep());
        response.setFailureReason(sagaOrder.getFailureReason());
        response.setCreatedAt(sagaOrder.getCreatedAt());
        response.setUpdatedAt(sagaOrder.getUpdatedAt());

        return response;
    }
}
