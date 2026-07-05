package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.OrderDeliveredEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveredConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.order-delivered.event",
            groupId = "orchestrator-group",
            containerFactory = "orderDeliveredKafkaListenerFactory"
    )
    public void consume(OrderDeliveredEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] OrderDeliveredEvent received. OrderNumber={}. Saga complete.", event.getOrderNumber());

        sagaOrchestratorService.onOrderDelivered(event);

        ack.acknowledge();
    }
}