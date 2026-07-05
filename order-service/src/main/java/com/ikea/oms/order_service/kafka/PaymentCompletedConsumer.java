package com.ikea.oms.order_service.kafka;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.event.PaymentCompletedEvent;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "payment-completed-topic",
            groupId = "order-group-v2",
            containerFactory = "paymentCompletedKafkaListenerFactory"
    )
    public void consume(PaymentCompletedEvent event) {

        log.info("========== Order Service ==========");

        log.info("PaymentCompletedEvent Received : {}", event);

        Order order = orderRepository
                .findById(event.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order Not Found : " + event.getOrderId()));

        order.setStatus(OrderStatus.PAYMENT_COMPLETED);

        orderRepository.save(order);

        log.info(
                "Order Status Updated Successfully. OrderNumber={} Status={}",
                order.getOrderNumber(),
                order.getStatus()
        );

        log.info("==================================");
    }
}