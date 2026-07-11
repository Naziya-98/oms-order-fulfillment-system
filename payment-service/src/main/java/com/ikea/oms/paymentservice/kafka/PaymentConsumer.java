package com.ikea.oms.paymentservice.kafka;

import com.ikea.oms.paymentservice.event.OrderCreatedEvent;
import com.ikea.oms.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

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

        // Payment success/failure is now decided by the simulated payment
        // gateway (PaymentGatewaySimulator), not by inspecting skuCode.
        paymentService.processPayment(event);

        log.info("=====================================");
    }
}
