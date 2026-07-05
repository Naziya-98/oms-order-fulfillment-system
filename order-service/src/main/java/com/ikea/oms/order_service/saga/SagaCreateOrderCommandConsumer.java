package com.ikea.oms.order_service.saga;

import com.ikea.oms.order_service.dto.InventoryResponseDTO;
import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderItem;
import com.ikea.oms.order_service.entity.OrderStatus;
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

    @KafkaListener(
            topics = "saga.create-order.command",
            groupId = "order-orchestration-group",
            containerFactory = "sagaCreateOrderKafkaListenerFactory"
    )
    public void consume(CreateOrderCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Order Service (Orchestrated) ==========");
        log.info("CreateOrderCommand received. OrderNumber={}, SKU={}, Quantity={}",
                command.getOrderNumber(), command.getSkuCode(), command.getQuantity());

        Order order = new Order();

        order.setOrderNumber(command.getOrderNumber());
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

        ack.acknowledge();
    }
}