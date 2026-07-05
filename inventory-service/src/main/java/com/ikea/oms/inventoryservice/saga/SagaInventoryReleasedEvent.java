package com.ikea.oms.inventoryservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.inventory-released.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaInventoryReleasedEvent {

    private Long orderId;
    private String orderNumber;
    private String inventoryStatus;
}
