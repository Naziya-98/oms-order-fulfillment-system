package com.ikea.oms.orchestratorservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.cancel-payment.command
// Consumed by payment-service (SagaCancelPaymentCommandConsumer).
// This is the real "cancel payment" endpoint Ashutosh asked for.
// Payment-service's response is deliberately published back on the SAME
// saga.payment-failed.event topic used by the automatic decline path, so
// no new consumer/wiring is needed on the orchestrator side - the existing
// PaymentFailedConsumer -> onPaymentFailed() already does the right thing
// (release inventory, mark CANCELLED), regardless of whether the payment
// failed at the gateway or was cancelled by request.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelPaymentCommand {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
