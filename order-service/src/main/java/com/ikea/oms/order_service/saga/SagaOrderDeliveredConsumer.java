package com.ikea.oms.order_service.saga;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrderDeliveredConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "saga.order-delivered.event",
            groupId = "order-orchestration-group",
            containerFactory = "sagaOrderDeliveredKafkaListenerFactory"
    )
    public void consume(SagaOrderDeliveredEvent event, Acknowledgment ack) {

        log.info("[SAGA] OrderDeliveredEvent received. OrderNumber={}", event.getOrderNumber());

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
            log.info("[SAGA] Order status updated to DELIVERED. OrderNumber={}", event.getOrderNumber());
        }, () -> log.warn("[SAGA] No order found for OrderNumber={}", event.getOrderNumber()));

        ack.acknowledge();
    }
}