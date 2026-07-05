package com.ikea.oms.order_service.kafka;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.event.OrderDeliveredEvent;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveredConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "order-delivered-topic",
            groupId = "order-group-v2",
            containerFactory = "deliveredKafkaListenerFactory"
    )
    public void consume(OrderDeliveredEvent event) {

        log.info("========== Order Service ==========");

        log.info("OrderDeliveredEvent Received : {}", event);

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order Not Found"));

        order.setStatus(OrderStatus.DELIVERED);

        orderRepository.save(order);

        log.info(
                "Order Status Updated Successfully. OrderNumber={} Status={}",
                order.getOrderNumber(),
                order.getStatus()
        );

        log.info("==================================");
    }
}