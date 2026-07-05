package com.ikea.oms.paymentservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

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
    public void consume(ProcessPaymentCommand command, Acknowledgment ack) {

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

        ack.acknowledge();
    }
}