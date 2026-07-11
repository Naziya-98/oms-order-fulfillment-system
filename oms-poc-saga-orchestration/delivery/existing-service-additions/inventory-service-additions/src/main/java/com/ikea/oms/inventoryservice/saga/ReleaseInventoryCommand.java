package com.ikea.oms.inventoryservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.release-inventory.command (compensation command from orchestrator-service)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseInventoryCommand {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
