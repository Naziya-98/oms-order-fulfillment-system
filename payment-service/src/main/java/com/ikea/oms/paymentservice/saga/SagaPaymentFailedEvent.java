package com.ikea.oms.paymentservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.payment-failed.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaPaymentFailedEvent {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String paymentStatus;
}
