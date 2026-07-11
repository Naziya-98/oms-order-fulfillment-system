package com.ikea.oms.order_service.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaInventoryReleasedEvent {
    private Long orderId;
    private String orderNumber;

    // The Order entity's own orderNumber field is this business number
    // (ORD...), not the saga's orderNumber (SAGA...) above — use this one
    // to look up the local order row.
    private String businessOrderNumber;

    private String inventoryStatus;
}