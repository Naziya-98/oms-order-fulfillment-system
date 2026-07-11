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
public class SagaInventoryReleasedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "saga.inventory-released.event",
            groupId = "order-orchestration-group",
            containerFactory = "sagaInventoryReleasedKafkaListenerFactory"
    )
    public void consume(SagaInventoryReleasedEvent event, Acknowledgment ack) {

        log.info("[SAGA] InventoryReleasedEvent received. sagaOrderId={}, sagaOrderNumber={}, businessOrderNumber={}",
                event.getOrderId(), event.getOrderNumber(), event.getBusinessOrderNumber());

        // Prefer sagaOrderId (stamped on the row at creation time — always present,
        // no race). Cancel can legitimately race ahead of order creation if the
        // customer cancels right after INVENTORY_RESERVED, before order-service's
        // CreateOrderCommand round-trip finishes — in that narrow window
        // businessOrderNumber may still be blank, so fall back to it, and finally to
        // the raw saga orderNumber as a last resort so nothing is silently dropped.
        var orderOpt = orderRepository.findBySagaOrderId(event.getOrderId());

        if (orderOpt.isEmpty() && event.getBusinessOrderNumber() != null && !event.getBusinessOrderNumber().isBlank()) {
            orderOpt = orderRepository.findByOrderNumber(event.getBusinessOrderNumber());
        }

        if (orderOpt.isEmpty()) {
            orderOpt = orderRepository.findByOrderNumber(event.getOrderNumber());
        }

        orderOpt.ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("[SAGA] Order status updated to CANCELLED. OrderNumber={}", order.getOrderNumber());
        }, () -> log.warn("[SAGA] No order found for sagaOrderId={}, businessOrderNumber={}, sagaOrderNumber={}",
                event.getOrderId(), event.getBusinessOrderNumber(), event.getOrderNumber()));

        ack.acknowledge();
    }
}