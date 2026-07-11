package com.ikea.oms.paymentservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * ORCHESTRATION-MODE listener. This is a parallel path to the existing
 * choreography PaymentConsumer (which reacts to order-created-topic).
 * This one only fires when the orchestrator-service explicitly commands it,
 * on a completely separate topic, so it cannot interfere with your
 * existing choreography demo.
 *
 * Reuses the exact same FAIL_PAYMENT simulation rule as the choreography flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaPaymentCommandConsumer {

    private final SagaPaymentEventProducer sagaPaymentEventProducer;

    @KafkaListener(
            topics = "saga.process-payment.command",
            groupId = "payment-orchestration-group",
            containerFactory = "sagaProcessPaymentKafkaListenerFactory"
    )
    public void consume(ProcessPaymentCommand command) {

        log.info("========== [SAGA] Payment Service (Orchestrated) ==========");

        log.info("ProcessPaymentCommand received for OrderNumber={}", command.getOrderNumber());

        if ("FAIL_PAYMENT".equalsIgnoreCase(command.getSkuCode())) {

            log.error("[SAGA] Payment failed for OrderNumber={}", command.getOrderNumber());

            SagaPaymentFailedEvent failedEvent = new SagaPaymentFailedEvent(
                    command.getOrderId(),
                    command.getOrderNumber(),
                    command.getSkuCode(),
                    command.getQuantity(),
                    "PAYMENT_FAILED"
            );

            sagaPaymentEventProducer.publishPaymentFailed(failedEvent);

        } else {

            log.info("[SAGA] Payment completed successfully for OrderNumber={}", command.getOrderNumber());

            SagaPaymentCompletedEvent completedEvent = new SagaPaymentCompletedEvent(
                    command.getOrderId(),
                    command.getOrderNumber(),
                    "PAYMENT_COMPLETED"
            );

            sagaPaymentEventProducer.publishPaymentCompleted(completedEvent);
        }

        log.info("=============================================================");
    }
}
