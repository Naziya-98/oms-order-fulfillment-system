package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Published to topic: saga.send CreateOrder.command
// Consumed by: order-service (SagaCreateOrderCommandConsumer)
@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateOrderCommand {

    private Long sagaOrderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
}