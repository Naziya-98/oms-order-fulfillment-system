package com.ikea.oms.orchestratorservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.payment-failed.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFailedEvent {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String paymentStatus;
}
