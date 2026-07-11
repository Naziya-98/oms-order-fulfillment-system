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
public class SagaReserveInventoryCommandConsumer {

    private final InventoryService inventoryService;
    private final SagaInventoryEventProducer sagaInventoryEventProducer;

    @KafkaListener(
            topics = "saga.reserve-inventory.command",
            groupId = "inventory-orchestration-group",
            containerFactory = "sagaReserveInventoryKafkaListenerFactory"
    )
    public void consume(ReserveInventoryCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Inventory Reservation (Orchestrated) ==========");

        log.info("ReserveInventoryCommand received for OrderNumber={}, SKU={}, Quantity={}", 
                 command.getOrderNumber(), command.getSkuCode(), command.getQuantity());

        try {

            var inventoryResponse = inventoryService.getInventoryBySkuCode(command.getSkuCode());

            if (command.getQuantity() > inventoryResponse.getQuantity()) {
                
                log.warn("Insufficient inventory for SKU={}, Available={}, Requested={}", 
                         command.getSkuCode(), inventoryResponse.getQuantity(), command.getQuantity());

                SagaInventoryReservedEvent failureEvent = new SagaInventoryReservedEvent(
                        command.getOrderId(),
                        command.getOrderNumber(),
                        command.getSkuCode(),
                        command.getQuantity(),
                        null,
                        "RESERVATION_FAILED"
                );

                sagaInventoryEventProducer.publishInventoryReserved(failureEvent);

            } else {

                // Reservation-based reserve (tracked, releasable by orderNumber),
                // instead of the old blind quantity deduction.
                inventoryService.reserveForOrder(command.getOrderNumber(), command.getSkuCode(), command.getQuantity());

                SagaInventoryReservedEvent successEvent = new SagaInventoryReservedEvent(
                        command.getOrderId(),
                        command.getOrderNumber(),
                        command.getSkuCode(),
                        command.getQuantity(),
                        inventoryResponse.getUnitPrice(),
                        "INVENTORY_RESERVED"
                );

                sagaInventoryEventProducer.publishInventoryReserved(successEvent);

                log.info("Inventory reserved successfully for OrderNumber={}, RemainingQuantity={}", 
                         command.getOrderNumber(), inventoryResponse.getQuantity() - command.getQuantity());
            }

        } catch (Exception e) {

            log.error("Inventory reservation failed for OrderNumber={}", command.getOrderNumber(), e);

            SagaInventoryReservedEvent failureEvent = new SagaInventoryReservedEvent(
                    command.getOrderId(),
                    command.getOrderNumber(),
                    command.getSkuCode(),
                    command.getQuantity(),
                    null,
                    "RESERVATION_FAILED"
            );

            sagaInventoryEventProducer.publishInventoryReserved(failureEvent);
        }

        log.info("====================================================================");

        ack.acknowledge();
    }
}
