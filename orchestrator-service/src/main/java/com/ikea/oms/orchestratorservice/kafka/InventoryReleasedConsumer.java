package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.InventoryReleasedEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReleasedConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.inventory-released.event",
            groupId = "orchestrator-group",
            containerFactory = "inventoryReleasedKafkaListenerFactory"
    )
    public void consume(InventoryReleasedEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] InventoryReleasedEvent received. OrderNumber={}. Compensation complete.", event.getOrderNumber());

        sagaOrchestratorService.onInventoryReleased(event);

        ack.acknowledge();
    }
}