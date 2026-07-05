package com.ikea.oms.orchestratorservice.service;
import com.ikea.oms.orchestratorservice.command.CreateOrderCommand;
import com.ikea.oms.orchestratorservice.command.ProcessPaymentCommand;
import com.ikea.oms.orchestratorservice.command.ReleaseInventoryCommand;
import com.ikea.oms.orchestratorservice.command.SendNotificationCommand;
import com.ikea.oms.orchestratorservice.dto.InventoryResponseDTO;
import com.ikea.oms.orchestratorservice.dto.OrderRequestDTO;
import com.ikea.oms.orchestratorservice.dto.OrderResponseDTO;
import com.ikea.oms.orchestratorservice.entity.SagaOrder;
import com.ikea.oms.orchestratorservice.entity.SagaStatus;
import com.ikea.oms.orchestratorservice.event.InventoryReleasedEvent;
import com.ikea.oms.orchestratorservice.event.NotificationSentEvent;
import com.ikea.oms.orchestratorservice.event.OrderDeliveredEvent;
import com.ikea.oms.orchestratorservice.event.PaymentCompletedEvent;
import com.ikea.oms.orchestratorservice.event.PaymentFailedEvent;
import com.ikea.oms.orchestratorservice.exception.InsufficientInventoryException;
import com.ikea.oms.orchestratorservice.exception.InventoryNotFoundException;
import com.ikea.oms.orchestratorservice.exception.SagaOrderNotFoundException;
import com.ikea.oms.orchestratorservice.kafka.SagaCommandProducer;
import com.ikea.oms.orchestratorservice.repository.SagaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * SAGA ORCHESTRATION (central coordinator)
 * ------------------------------------------------------------------
 * Unlike the choreography flow (order/inventory/payment/notification
 * services autonomously reacting to each other's domain events), this
 * class is the single source of truth for "what happens next".
 *
 * It:
 *   1) Reserves inventory synchronously (REST call, same as order-service today)
 *   2) Explicitly commands payment-service to process payment
 *   3) On success, explicitly commands notification-service to notify + deliver
 *   4) On failure, explicitly commands inventory-service to compensate (release stock)
 *
 * All state transitions are persisted in saga_orders so progress can be
 * inspected via GET /api/orchestrated-orders/{orderNumber}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorService {

    private final SagaOrderRepository sagaOrderRepository;
    private final WebClient inventoryWebClient;
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

        // ---- Step 1: reserve inventory (synchronous, same as choreography's order-service) ----

        InventoryResponseDTO inventoryResponse;

        try {
            log.info("[ORCHESTRATOR] Checking inventory for SKU={}", request.getSkuCode());

            inventoryResponse = inventoryWebClient.get()
                    .uri("/api/inventory/" + request.getSkuCode())
                    .retrieve()
                    .bodyToMono(InventoryResponseDTO.class)
                    .block();

        } catch (Exception e) {
            log.error("[ORCHESTRATOR] Inventory lookup failed for SKU={}", request.getSkuCode(), e);
            throw new InventoryNotFoundException("Inventory not found for SKU: " + request.getSkuCode());
        }

        if (inventoryResponse == null) {
            throw new InventoryNotFoundException("Inventory not found for SKU: " + request.getSkuCode());
        }

        if (request.getQuantity() > inventoryResponse.getQuantity()) {
            throw new InsufficientInventoryException("Insufficient Inventory Available");
        }

        sagaOrder.setUnitPrice(inventoryResponse.getUnitPrice());

        InventoryResponseDTO updatedInventory = inventoryWebClient.patch()
                .uri("/api/inventory/" + request.getSkuCode() + "/" + request.getQuantity())
                .retrieve()
                .bodyToMono(InventoryResponseDTO.class)
                .block();

        if (updatedInventory != null) {
            log.info("[ORCHESTRATOR] Inventory reserved. RemainingQuantity={}", updatedInventory.getQuantity());
        }

        sagaOrder.setStatus(SagaStatus.INVENTORY_RESERVED);
        sagaOrder.setCurrentStep("INVENTORY_RESERVED");

        SagaOrder savedOrder = sagaOrderRepository.save(sagaOrder);

        try {
            log.info("[TEST] Sleeping 10s after INVENTORY_RESERVED save — kill the app now to test the resumability gap.");
            Thread.sleep(10000); // TEMPORARY — remove after this test
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[ORCHESTRATOR] SagaOrder persisted. Id={}, OrderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());


        // ---- Step 2: explicitly command order-service to persist the order record ----
        CreateOrderCommand createOrderCommand = new CreateOrderCommand(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getSkuCode(),
                savedOrder.getQuantity(),
                savedOrder.getUnitPrice(),
                savedOrder.getCustomerName(),
                savedOrder.getCustomerEmail(),
                savedOrder.getShippingAddress()
        );

        sagaCommandProducer.sendCreateOrderCommand(createOrderCommand);

        // ---- Step 3: explicitly command payment-service ----

        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getSkuCode(),
                savedOrder.getQuantity()
        );

        sagaCommandProducer.sendProcessPaymentCommand(paymentCommand);

        savedOrder.setStatus(SagaStatus.PAYMENT_PROCESSING);
        savedOrder.setCurrentStep("PROCESS_PAYMENT_COMMAND_SENT");
        sagaOrderRepository.save(savedOrder);

        return toResponse(savedOrder);
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
                sagaOrder.getSkuCode(),
                sagaOrder.getQuantity()
        );

        sagaCommandProducer.sendReleaseInventoryCommand(releaseCommand);

        sagaOrder.setStatus(SagaStatus.COMPENSATING);
        sagaOrder.setCurrentStep("RELEASE_INVENTORY_COMMAND_SENT");
        sagaOrderRepository.save(sagaOrder);
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
