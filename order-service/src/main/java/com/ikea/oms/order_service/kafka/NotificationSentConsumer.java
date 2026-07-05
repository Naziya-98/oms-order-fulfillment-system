package com.ikea.oms.order_service.kafka;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.event.NotificationSentEvent;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSentConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "notification-sent-topic",
            groupId = "order-group-v2",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void consume(NotificationSentEvent event) {

        log.info("========== Order Service ==========");

        log.info("NotificationSentEvent Received : {}", event);

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order Not Found"));

        order.setStatus(OrderStatus.NOTIFICATION_SENT);

        orderRepository.save(order);

        log.info(
                "Order Status Updated Successfully. OrderNumber={} Status={}",
                order.getOrderNumber(),
                order.getStatus()
        );

        log.info("==================================");
    }
}