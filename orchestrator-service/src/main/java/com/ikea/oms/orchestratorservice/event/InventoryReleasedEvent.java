package com.ikea.oms.orchestratorservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.inventory-released.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReleasedEvent {

    private Long orderId;
    private String orderNumber;
    private String inventoryStatus;
}
