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
public class SagaNotificationSentConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "saga.notification-sent.event",
            groupId = "order-orchestration-group",
            containerFactory = "sagaNotificationSentKafkaListenerFactory"
    )
    public void consume(SagaNotificationSentEvent event, Acknowledgment ack) {

        log.info("[SAGA] NotificationSentEvent received. sagaOrderId={}, sagaOrderNumber={}",
                event.getOrderId(), event.getOrderNumber());

        // Same root cause as SagaPaymentCompletedConsumer: correlate on the saga's
        // numeric orderId (stamped as sagaOrderId on the row at creation time), not
        // the saga's SAGA... orderNumber string, which this table doesn't use as a key.
        orderRepository.findBySagaOrderId(event.getOrderId()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.NOTIFICATION_SENT);
            orderRepository.save(order);
            log.info("[SAGA] Order status updated to NOTIFICATION_SENT. OrderNumber={}", order.getOrderNumber());
        }, () -> log.warn("[SAGA] No order found for sagaOrderId={} (sagaOrderNumber={})",
                event.getOrderId(), event.getOrderNumber()));

        ack.acknowledge();
    }
}