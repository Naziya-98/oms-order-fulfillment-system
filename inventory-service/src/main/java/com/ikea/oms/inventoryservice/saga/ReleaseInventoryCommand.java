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

    // Order Service's own business orderNumber (ORD...), passed through so it
    // can be forwarded on SagaInventoryReleasedEvent — inventory-service itself
    // doesn't need it (its own Reservation rows are keyed by the saga orderNumber),
    // but order-service does, to find its local order row.
    private String businessOrderNumber;

    private String skuCode;
    private Integer quantity;
}
