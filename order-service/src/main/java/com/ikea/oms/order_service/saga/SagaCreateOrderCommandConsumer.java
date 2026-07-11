package com.ikea.oms.order_service.saga;

import com.ikea.oms.order_service.dto.InventoryResponseDTO;
import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderItem;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.event.OrderCreatedEvent;
import com.ikea.oms.order_service.kafka.OrderProducer;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCreateOrderCommandConsumer {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final OrderProducer orderProducer;

    @KafkaListener(
            topics = "saga.create-order.command",
            groupId = "order-orchestration-group",
            containerFactory = "sagaCreateOrderKafkaListenerFactory"
    )
    public void consume(CreateOrderCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Order Service (Orchestrated) ==========");
        log.info("CreateOrderCommand received. sagaOrderId={}, SKU={}, Quantity={}",
                command.getSagaOrderId(), command.getSkuCode(), command.getQuantity());

        Order order = new Order();

        // Let Order Service generate its own business orderNumber if not provided
        String businessOrderNumber = command.getOrderNumber();
        if (businessOrderNumber == null || businessOrderNumber.isEmpty()) {
            businessOrderNumber = "ORD" + System.currentTimeMillis();
        }

        order.setOrderNumber(businessOrderNumber);
        // Store the saga's numeric id on the row itself so every later saga.*.event
        // (payment-completed, notification-sent, order-delivered, inventory-released)
        // can find this row via orderId alone, with no dependency on businessOrderNumber
        // having made it back from the orchestrator yet.
        order.setSagaOrderId(command.getSagaOrderId());
        order.setCustomerName(command.getCustomerName());
        order.setCustomerEmail(command.getCustomerEmail());
        order.setShippingAddress(command.getShippingAddress());
        order.setStatus(OrderStatus.INVENTORY_RESERVED);

        String productName = command.getSkuCode();

        try {
            InventoryResponseDTO inventoryResponse = webClient.get()
                    .uri("/api/inventory/" + command.getSkuCode())
                    .retrieve()
                    .bodyToMono(InventoryResponseDTO.class)
                    .block();

            if (inventoryResponse != null && inventoryResponse.getProductName() != null) {
                productName = inventoryResponse.getProductName();
            }
        } catch (Exception e) {
            log.warn("Could not enrich productName for SKU={}, defaulting to SKU code", command.getSkuCode());
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setSkuCode(command.getSkuCode());
        orderItem.setProductName(productName);
        orderItem.setUnitPrice(command.getUnitPrice());
        orderItem.setQuantity(command.getQuantity());
        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);

        Order savedOrder = orderRepository.save(order);

        log.info("[SAGA] Order persisted successfully. OrderId={}, OrderNumber={}, Status={}",
                savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getStatus());
        log.info("===========================================================");

        // NOTE: orchestrated orders must NOT be published on "order-created-topic" —
        // that topic is the choreography pipeline's trigger, and payment-service /
        // inventory-service's choreography consumers listen on it. Publishing there
        // for an orchestrated order caused an orphaned Payment row (and duplicate log
        // noise in inventory-service) for orders that are actually being driven by
        // the orchestrator via explicit saga.* commands. Only publish the saga-specific
        // event so the orchestrator can correlate and update saga state.
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                command.getSagaOrderId(),
                command.getSkuCode(),
                command.getQuantity()
        );

        orderProducer.publishSagaOrderCreated(orderCreatedEvent);

        ack.acknowledge();
    }
}