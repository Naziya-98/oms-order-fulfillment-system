package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.process-payment.command
// Consumed by: payment-service (SagaPaymentCommandConsumer)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessPaymentCommand {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
