package com.ikea.oms.notificationservice.kafka;

import com.ikea.oms.notificationservice.event.NotificationSentEvent;
import com.ikea.oms.notificationservice.event.OrderDeliveredEvent;
import com.ikea.oms.notificationservice.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationProducer notificationProducer;

    @KafkaListener(
            topics = "payment-completed-topic",
            groupId = "notification-group"
    )
    public void consume(PaymentCompletedEvent event) throws InterruptedException {

        log.info("========== Notification Service ==========");

        log.info(
                "Payment completed for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info("Preparing customer notifications...");

        log.info("Email sent successfully.");

        log.info("SMS sent successfully.");

        Thread.sleep(5000);

        NotificationSentEvent notificationEvent =
                new NotificationSentEvent(
                        event.getOrderId(),
                        event.getOrderNumber(),
                        "NOTIFICATION_SENT"
                );

        notificationProducer.publishNotificationSentEvent(notificationEvent);

        log.info(
                "NotificationSentEvent published for OrderNumber={}",
                event.getOrderNumber()
        );

        Thread.sleep(5000);

        OrderDeliveredEvent deliveredEvent =
                new OrderDeliveredEvent(
                        event.getOrderId(),
                        event.getOrderNumber(),
                        "DELIVERED"
                );

        notificationProducer.publishDeliveredEvent(deliveredEvent);

        log.info(
                "OrderDeliveredEvent published for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info("==========================================");
    }
}