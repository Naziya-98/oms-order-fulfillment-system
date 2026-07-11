package com.ikea.oms.paymentservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.cancel-payment.command (sent by orchestrator-service)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPaymentCommand {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
