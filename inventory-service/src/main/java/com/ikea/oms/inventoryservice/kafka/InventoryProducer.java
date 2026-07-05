package com.ikea.oms.inventoryservice.kafka;

import com.ikea.oms.inventoryservice.event.InventoryReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryReleasedEvent(
            InventoryReleasedEvent event) {

        log.info("Publishing InventoryReleasedEvent {}", event);

        kafkaTemplate.send(
                "inventory-released-topic",
                event
        );

        log.info("InventoryReleasedEvent Published Successfully");
    }
}