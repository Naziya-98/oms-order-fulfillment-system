package com.ikea.oms.notificationservice.kafka;
import com.ikea.oms.notificationservice.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @KafkaListener(topics = "order-created-topic",
            groupId = "notification-group")

    public void consume(OrderCreatedEvent event) {

        log.info("========== Notification Service ==========");

        log.info(
                "Notification triggered for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info(
                "Order Details: {}",
                event
        );

        log.info(
                "Notification sent successfully for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info("==========================================");
    }
}
