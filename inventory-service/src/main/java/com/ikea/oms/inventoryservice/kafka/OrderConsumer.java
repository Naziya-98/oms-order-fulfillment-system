package com.ikea.oms.inventoryservice.kafka;

import com.ikea.oms.inventoryservice.event.InventoryReleasedEvent;
import com.ikea.oms.inventoryservice.event.OrderCreatedEvent;
import com.ikea.oms.inventoryservice.event.PaymentFailedEvent;
import com.ikea.oms.inventoryservice.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumer {

    private final InventoryService inventoryService;
    private final InventoryProducer inventoryProducer;

    public OrderConsumer(
            InventoryService inventoryService,
            InventoryProducer inventoryProducer) {

        this.inventoryService = inventoryService;
        this.inventoryProducer = inventoryProducer;
    }

    @KafkaListener(
            topics = "order-created-topic",
            groupId = "inventory-group",
            containerFactory = "orderCreatedKafkaListenerFactory"
    )
    public void consume(OrderCreatedEvent event) {

        log.info("OrderCreatedEvent received");

        log.info(
                "OrderId={} OrderNumber={} SKU={} Quantity={}",
                event.getOrderId(),
                event.getOrderNumber(),
                event.getSkuCode(),
                event.getQuantity()
        );

        log.info("Event processed successfully");
    }


    // Compensation Listener

    @KafkaListener(
            topics = "payment-failed-topic",
            groupId = "inventory-group",
            containerFactory = "paymentFailedKafkaListenerFactory"
    )
    public void consume(PaymentFailedEvent event) {

        log.info("========== Inventory Compensation ==========");

        log.info(
                "PaymentFailedEvent received for OrderNumber={}",
                event.getOrderNumber()
        );

        // Wait before releasing inventory so the reserved quantity
        log.info("Waiting 5 seconds before releasing inventory...");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while waiting.", e);
        }

        inventoryService.releaseForOrder(event.getOrderNumber());

        InventoryReleasedEvent inventoryReleasedEvent =
                new InventoryReleasedEvent(
                        event.getOrderId(),
                        event.getOrderNumber(),
                        "INVENTORY_RELEASED"
                );

        inventoryProducer.publishInventoryReleasedEvent(
                inventoryReleasedEvent
        );

        log.info(
                "InventoryReleasedEvent published for OrderNumber={}",
                event.getOrderNumber()
        );

        log.info("============================================");
    }
}