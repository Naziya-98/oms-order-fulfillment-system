package com.ikea.oms.notificationservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaNotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishNotificationSent(SagaNotificationSentEvent event) {

        log.info("[SAGA] Publishing SagaNotificationSentEvent {}", event);

        kafkaTemplate.send("saga.notification-sent.event", event);
    }

    public void publishOrderDelivered(SagaOrderDeliveredEvent event) {

        log.info("[SAGA] Publishing SagaOrderDeliveredEvent {}", event);

        kafkaTemplate.send("saga.order-delivered.event", event);
    }
}
