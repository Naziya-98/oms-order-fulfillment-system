package com.ikea.oms.inventoryservice.saga;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

// Additive config, separate from the existing KafkaListenerConfig in this service.
@Configuration
public class SagaKafkaListenerConfig {

    private Map<String, Object> commonProps() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-orchestration-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReleaseInventoryCommand>
    sagaReleaseInventoryKafkaListenerFactory() {

        JsonDeserializer<ReleaseInventoryCommand> deserializer =
                new JsonDeserializer<>(ReleaseInventoryCommand.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, ReleaseInventoryCommand> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                new DefaultKafkaConsumerFactory<>(
                        commonProps(),
                        new StringDeserializer(),
                        deserializer
                )
        );

        return factory;
    }
}
