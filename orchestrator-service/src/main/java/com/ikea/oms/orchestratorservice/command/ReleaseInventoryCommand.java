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
    private String skuCode;
    private Integer quantity;
}
