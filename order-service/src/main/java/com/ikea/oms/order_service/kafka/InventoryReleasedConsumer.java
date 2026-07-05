package com.ikea.oms.order_service.kafka;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.event.InventoryReleasedEvent;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReleasedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "inventory-released-topic",
            groupId = "order-group-v2",
            containerFactory = "inventoryReleasedKafkaListenerFactory"
    )
    public void consume(InventoryReleasedEvent event) {

        log.info("========== Order Compensation ==========");

        log.info(
                "InventoryReleasedEvent Received : {}",
                event
        );

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order Not Found : " + event.getOrderId()));

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        log.info(
                "Order Compensation Completed. OrderNumber={} Status={}",
                order.getOrderNumber(),
                order.getStatus()
        );

        log.info("========================================");
    }
}