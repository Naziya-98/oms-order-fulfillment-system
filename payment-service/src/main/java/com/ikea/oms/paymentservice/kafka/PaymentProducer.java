package com.ikea.oms.paymentservice.kafka;

import com.ikea.oms.paymentservice.event.PaymentCompletedEvent;
import com.ikea.oms.paymentservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompletedEvent(
            PaymentCompletedEvent event) {

        log.info("Publishing PaymentCompletedEvent {}", event);

        kafkaTemplate.send(
                "payment-completed-topic",
                event
        );

        log.info("PaymentCompletedEvent Published Successfully");
    }


    // NEW METHOD

    public void publishPaymentFailedEvent(
            PaymentFailedEvent event) {

        log.info("Publishing PaymentFailedEvent {}", event);

        kafkaTemplate.send(
                "payment-failed-topic",
                event
        );

        log.info("PaymentFailedEvent Published Successfully");
    }
}