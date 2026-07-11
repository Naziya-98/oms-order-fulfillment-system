package com.ikea.oms.paymentservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaPaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(SagaPaymentCompletedEvent event) {

        log.info("[SAGA] Publishing SagaPaymentCompletedEvent {}", event);

        kafkaTemplate.send("saga.payment-completed.event", event);
    }

    public void publishPaymentFailed(SagaPaymentFailedEvent event) {

        log.info("[SAGA] Publishing SagaPaymentFailedEvent {}", event);

        kafkaTemplate.send("saga.payment-failed.event", event);
    }
}
