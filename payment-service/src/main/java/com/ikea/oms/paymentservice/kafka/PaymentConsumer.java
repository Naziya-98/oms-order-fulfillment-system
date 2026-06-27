package com.ikea.oms.paymentservice.kafka;
import com.ikea.oms.paymentservice.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentConsumer {

    @KafkaListener(
            topics = "order-created-topic",
            groupId = "payment-group"
    )
    public void consume(OrderCreatedEvent event) {

        log.info("========== Payment Service ==========");

        log.info(
                "Payment processing started for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info(
                "Order Details: {}",
                event
        );

        log.info(
                "Payment completed successfully for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info("=====================================");
    }
}
