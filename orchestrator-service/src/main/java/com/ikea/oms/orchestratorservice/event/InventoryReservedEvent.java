package com.ikea.oms.orchestratorservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Consumed from topic: saga.inventory-reserved.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservedEvent {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String status;
}
