package com.ikea.oms.orchestratorservice.kafka;

import com.ikea.oms.orchestratorservice.event.SagaInventoryReservedEvent;
import com.ikea.oms.orchestratorservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservedConsumer {

    private final SagaOrchestratorService sagaOrchestratorService;

    @KafkaListener(
            topics = "saga.inventory-reserved.event",
            groupId = "orchestrator-group",
            containerFactory = "sagaInventoryReservedKafkaListenerFactory"
    )
    public void consume(SagaInventoryReservedEvent event, Acknowledgment ack) {

        log.info("[ORCHESTRATOR] SagaInventoryReservedEvent received. OrderNumber={}, Status={}", 
                 event.getOrderNumber(), event.getStatus());

        sagaOrchestratorService.onInventoryReserved(event);

        ack.acknowledge();
    }
}
