package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Published to topic: saga.reserve-inventory.command
// Consumed by: inventory-service (SagaReserveInventoryCommandConsumer)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveInventoryCommand {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
