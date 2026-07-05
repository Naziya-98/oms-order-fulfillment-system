package com.ikea.oms.notificationservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaSendNotificationCommandConsumer {

    private final SagaNotificationEventProducer sagaNotificationEventProducer;

    @KafkaListener(
            topics = "saga.send-notification.command",
            groupId = "notification-orchestration-group",
            containerFactory = "sagaSendNotificationKafkaListenerFactory"
    )
    public void consume(SendNotificationCommand command, Acknowledgment ack) throws InterruptedException {

        log.info("========== [SAGA] Notification Service (Orchestrated) ==========");

        log.info("SendNotificationCommand received for OrderNumber={}", command.getOrderNumber());

        log.info("Preparing customer notifications...");
        log.info("Email sent successfully.");
        log.info("SMS sent successfully.");

        Thread.sleep(5000);

        SagaNotificationSentEvent notificationEvent = new SagaNotificationSentEvent(
                command.getOrderId(),
                command.getOrderNumber(),
                "NOTIFICATION_SENT"
        );

        sagaNotificationEventProducer.publishNotificationSent(notificationEvent);

        log.info("SagaNotificationSentEvent published for OrderNumber={}", command.getOrderNumber());

        Thread.sleep(5000);

        SagaOrderDeliveredEvent deliveredEvent = new SagaOrderDeliveredEvent(
                command.getOrderId(),
                command.getOrderNumber(),
                "DELIVERED"
        );

        sagaNotificationEventProducer.publishOrderDelivered(deliveredEvent);

        log.info("SagaOrderDeliveredEvent published for OrderNumber={}", command.getOrderNumber());

        log.info("=========================================================================");

        ack.acknowledge();
    }
}