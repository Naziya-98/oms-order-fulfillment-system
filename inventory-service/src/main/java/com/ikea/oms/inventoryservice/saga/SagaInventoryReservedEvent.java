package com.ikea.oms.inventoryservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Published to topic: saga.inventory-reserved.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaInventoryReservedEvent {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String status;
}
