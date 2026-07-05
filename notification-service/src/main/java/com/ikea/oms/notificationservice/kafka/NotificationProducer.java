package com.ikea.oms.notificationservice.kafka;

import com.ikea.oms.notificationservice.event.NotificationSentEvent;
import com.ikea.oms.notificationservice.event.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void publishNotificationSentEvent(NotificationSentEvent event){

        log.info("Publishing NotificationSentEvent {}",event);

        kafkaTemplate.send(
                "notification-sent-topic",
                event
        );
    }

    public void publishDeliveredEvent(OrderDeliveredEvent event){

        log.info("Publishing OrderDeliveredEvent {}",event);

        kafkaTemplate.send(
                "order-delivered-topic",
                event
        );
    }
}
