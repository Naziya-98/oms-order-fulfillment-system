package com.ikea.oms.paymentservice.kafka;

import com.ikea.oms.paymentservice.event.OrderCreatedEvent;
import com.ikea.oms.paymentservice.event.PaymentCompletedEvent;
import com.ikea.oms.paymentservice.event.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentConsumer {

    private final PaymentProducer paymentProducer;

    public PaymentConsumer(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @KafkaListener(
            topics = "order-created-topic",
            groupId = "payment-group"
    )
    public void consume(OrderCreatedEvent event) {

        log.info("========== Payment Service ==========");

        log.info(
                "Payment processing started for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info(
                "Order Details: {}",
                event
        );


        // SIMULATE PAYMENT FAILURE USING SKU CODE
        // If skuCode = FAIL_PAYMENT, publish PaymentFailedEvent
        // Otherwise continue the existing success flow.


        if ("FAIL_PAYMENT".equalsIgnoreCase(event.getSkuCode())) {

            log.error(
                    "Payment failed for OrderNumber={}",
                    event.getOrderNumber()
            );

            PaymentFailedEvent paymentFailedEvent =
                    new PaymentFailedEvent(
                            event.getOrderId(),
                            event.getOrderNumber(),
                            event.getSkuCode(),
                            event.getQuantity(),
                            "PAYMENT_FAILED"
                    );

            paymentProducer.publishPaymentFailedEvent(paymentFailedEvent);

            log.info(
                    "PaymentFailedEvent published for OrderNumber={}",
                    event.getOrderNumber()
            );

        } else {

            log.info(
                    "Payment completed successfully for OrderNumber={}",
                    event.getOrderNumber()
            );

            PaymentCompletedEvent paymentCompletedEvent =
                    new PaymentCompletedEvent(
                            event.getOrderId(),
                            event.getOrderNumber(),
                            "PAYMENT_COMPLETED"
                    );

            paymentProducer.publishPaymentCompletedEvent(paymentCompletedEvent);

            log.info(
                    "PaymentCompletedEvent published for OrderNumber={}",
                    event.getOrderNumber()
            );
        }

        log.info("=====================================");
    }
}