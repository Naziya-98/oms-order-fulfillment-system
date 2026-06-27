package com.ikea.oms.order_service.kafka;
import com.ikea.oms.order_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProducer {
   private final KafkaTemplate<String ,Object> kafkaTemplate;

   public  void publishOrderEvent(OrderCreatedEvent event){

       log.info("Publishing OrderCreatedEvent. OrderNumber={}", event);

       kafkaTemplate.send("order-created-topic", event);

       log.info(
               "OrderCreatedEvent published successfully. OrderId={}, OrderNumber={}, SKU={}, Quantity={}",
               event.getOrderId(),
               event.getOrderNumber(),
               event.getSkuCode(),
               event.getQuantity()
       );

   }

}
