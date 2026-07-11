package com.ikea.oms.paymentservice.saga;

import com.ikea.oms.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

// Real "cancel payment" endpoint Ashutosh asked for, orchestration side.
// Deliberately publishes back on saga.payment-failed.event (SagaPaymentFailedEvent) -
// the SAME topic/type the automatic gateway-decline path uses - so the
// orchestrator's existing PaymentFailedConsumer/onPaymentFailed() handles the
// rest of the compensation (release inventory, mark CANCELLED) with no new
// wiring needed on that side.
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCancelPaymentCommandConsumer {

    private final SagaPaymentEventProducer sagaPaymentEventProducer;
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "saga.cancel-payment.command",
            groupId = "payment-orchestration-group",
            containerFactory = "sagaCancelPaymentKafkaListenerFactory"
    )
    public void consume(CancelPaymentCommand command, Acknowledgment ack) {

        log.info("========== [SAGA] Payment Service - Cancel Payment (Refund) ==========");
        log.info("CancelPaymentCommand received for OrderNumber={}", command.getOrderNumber());

        // Simulated refund - a real integration would call the gateway's
        // refund API here and branch on its response instead of always succeeding.
        log.info("[SAGA] Refund processed successfully for OrderNumber={}", command.getOrderNumber());

        paymentService.cancelOrchestratedPayment(command.getOrderNumber());

        sagaPaymentEventProducer.publishPaymentFailed(new SagaPaymentFailedEvent(
                command.getOrderId(),
                command.getOrderNumber(),
                command.getSkuCode(),
                command.getQuantity(),
                "PAYMENT_CANCELLED"
        ));

        log.info("========================================================================");

        ack.acknowledge();
    }
}
