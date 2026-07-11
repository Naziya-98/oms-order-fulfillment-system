package com.ikea.oms.inventoryservice.saga;

import com.ikea.oms.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaReleaseInventoryCommandConsumer {

    private final InventoryService inventoryService;
    private final SagaInventoryEventProducer sagaInventoryEventProducer;

    @KafkaListener(
            topics = "saga.release-inventory.command",
            groupId = "inventory-orchestration-group",
            containerFactory = "sagaReleaseInventoryKafkaListenerFactory"
    )
    public void consume(ReleaseInventoryCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Inventory Compensation (Orchestrated) ==========");

        log.info("ReleaseInventoryCommand received for OrderNumber={}", command.getOrderNumber());

        inventoryService.releaseForOrder(command.getOrderNumber());

        SagaInventoryReleasedEvent releasedEvent = new SagaInventoryReleasedEvent(
                command.getOrderId(),
                command.getOrderNumber(),
                command.getBusinessOrderNumber(),
                "INVENTORY_RELEASED"
        );

        sagaInventoryEventProducer.publishInventoryReleased(releasedEvent);

        log.info("InventoryReleasedEvent published for OrderNumber={}", command.getOrderNumber());

        log.info("====================================================================");

        ack.acknowledge();
    }
}