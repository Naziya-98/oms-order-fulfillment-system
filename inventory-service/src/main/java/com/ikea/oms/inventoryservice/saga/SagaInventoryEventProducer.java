package com.ikea.oms.inventoryservice.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaInventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryReserved(SagaInventoryReservedEvent event) {

        log.info("[SAGA] Publishing SagaInventoryReservedEvent {}", event);

        kafkaTemplate.send("saga.inventory-reserved.event", event);
    }

    public void publishInventoryReleased(SagaInventoryReleasedEvent event) {

        log.info("[SAGA] Publishing SagaInventoryReleasedEvent {}", event);

        kafkaTemplate.send("saga.inventory-released.event", event);
    }
}
