package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.OrderCreatedEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrderCreatedConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.order-created.event",
            groupId = "orchestrator-group",
            containerFactory = "orderCreatedKafkaListenerFactory"
    )
    public void consume(OrderCreatedEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] Saga OrderCreatedEvent received. sagaOrderId={}, orderNumber={}",
                 event.getSagaOrderId(), event.getOrderNumber());

        sagaOrchestratorService.onOrderCreated(event);
        ack.acknowledge();
    }
}
