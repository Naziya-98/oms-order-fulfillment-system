package com.ikea.oms.paymentservice.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PaymentCompletedEvent {
    private Long orderId;

    private String orderNumber;

    private String paymentStatus;

}







