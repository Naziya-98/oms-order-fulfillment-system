package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.PaymentFailedEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFailedConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.payment-failed.event",
            groupId = "orchestrator-group",
            containerFactory = "paymentFailedKafkaListenerFactory"
    )
    public void consume(PaymentFailedEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] PaymentFailedEvent received. OrderNumber={}. Triggering compensation.", event.getOrderNumber());

        sagaOrchestratorService.onPaymentFailed(event);

        ack.acknowledge();
    }
}