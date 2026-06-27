package com.ikea.oms.inventoryservice.kafka;
import com.ikea.oms.inventoryservice.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class OrderConsumer {
    @KafkaListener  (topics ="order-created-topic" , groupId= "Inventory-group")


    public void consume(OrderCreatedEvent event) {

       /* System.out.println("========== EVENT RECEIVED ==========");
        System.out.println("Order Id : " + event.getOrderId());
        System.out.println("Order Number : " + event.getOrderNumber());
        System.out.println("SKU : " + event.getSkuCode());
        System.out.println("Quantity : " + event.getQuantity());
        System.out.println("===================================");*/

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
}

