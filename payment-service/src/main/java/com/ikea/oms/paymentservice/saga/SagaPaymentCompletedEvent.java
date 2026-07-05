package com.ikea.oms.paymentservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.payment-completed.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaPaymentCompletedEvent {

    private Long orderId;
    private String orderNumber;
    private String paymentStatus;
}
