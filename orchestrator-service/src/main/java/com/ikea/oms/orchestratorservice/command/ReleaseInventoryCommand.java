package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.release-inventory.command
// Consumed by: inventory-service (SagaReleaseInventoryCommandConsumer)
// This is the COMPENSATION command issued by the orchestrator.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseInventoryCommand {

    private Long orderId;
    private String orderNumber;
    // order-service persists its OWN business orderNumber (ORD...) as the
    // Order entity's orderNumber field — it is NOT the same as the saga's
    // orderNumber (SAGA...) above. Without this, order-service's compensation
    // consumer looks up by the wrong key and silently finds nothing, so the
    // local order row never flips to CANCELLED.
    private String businessOrderNumber;
    private String skuCode;
    private Integer quantity;
}
