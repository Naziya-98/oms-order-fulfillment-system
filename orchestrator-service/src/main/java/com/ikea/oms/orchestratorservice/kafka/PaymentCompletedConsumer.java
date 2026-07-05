package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.PaymentCompletedEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.payment-completed.event",
            groupId = "orchestrator-group",
            containerFactory = "paymentCompletedKafkaListenerFactory"
    )
    public void consume(PaymentCompletedEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] PaymentCompletedEvent received. OrderNumber={}", event.getOrderNumber());

        sagaOrchestratorService.onPaymentCompleted(event);

        ack.acknowledge();
    }
}