package com.ikea.oms.order_service.saga;

import com.ikea.oms.order_service.entity.Order;
import com.ikea.oms.order_service.entity.OrderStatus;
import com.ikea.oms.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaPaymentCompletedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "saga.payment-completed.event",
            groupId = "order-orchestration-group",
            containerFactory = "sagaPaymentCompletedKafkaListenerFactory"
    )
    public void consume(SagaPaymentCompletedEvent event, Acknowledgment ack) {

        log.info("[SAGA] PaymentCompletedEvent received. sagaOrderId={}, sagaOrderNumber={}",
                event.getOrderId(), event.getOrderNumber());

        // event.getOrderNumber() is the saga's SAGA... orderNumber, not this
        // service's own ORD... business orderNumber — looking up by it here always
        // silently failed, which is why the local order row previously never left
        // INVENTORY_RESERVED. event.getOrderId() is the saga's numeric id, stamped
        // on the order row (sagaOrderId) the moment it was created, so it is always
        // safe to correlate on.
        orderRepository.findBySagaOrderId(event.getOrderId()).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
            orderRepository.save(order);
            log.info("[SAGA] Order status updated to PAYMENT_COMPLETED. OrderNumber={}", order.getOrderNumber());
        }, () -> log.warn("[SAGA] No order found for sagaOrderId={} (sagaOrderNumber={})",
                event.getOrderId(), event.getOrderNumber()));

        ack.acknowledge();
    }
}