package com.ikea.oms.inventoryservice.saga;

import com.ikea.oms.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * ORCHESTRATION-MODE compensation listener. Only fires when the
 * orchestrator-service explicitly commands a rollback, on a separate
 * topic from the existing choreography compensation path
 * (OrderConsumer.consume(PaymentFailedEvent) on payment-failed-topic),
 * so the existing choreography demo is untouched.
 *
 * Reuses the existing InventoryService.releaseInventory(...) method as-is.
 */
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
    public void consume(ReleaseInventoryCommand command) {

        log.info("========== [SAGA] Inventory Compensation (Orchestrated) ==========");

        log.info("ReleaseInventoryCommand received for OrderNumber={}", command.getOrderNumber());

        inventoryService.releaseInventory(command.getSkuCode(), command.getQuantity());

        SagaInventoryReleasedEvent releasedEvent = new SagaInventoryReleasedEvent(
                command.getOrderId(),
                command.getOrderNumber(),
                "INVENTORY_RELEASED"
        );

        sagaInventoryEventProducer.publishInventoryReleased(releasedEvent);

        log.info("InventoryReleasedEvent published for OrderNumber={}", command.getOrderNumber());

        log.info("====================================================================");
    }
}
