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

    // Forwarded from ReleaseInventoryCommand so order-service (which keys its
    // local orders table by this, not by the saga orderNumber) can find and
    // cancel the right row.
    private String businessOrderNumber;

    private String inventoryStatus;
}
