package com.ikea.oms.order_service.saga;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaKafkaListenerConfig {

    private Map<String, Object> commonProps() {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-orchestration-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // NEW

        return props;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(Class<T> clazz) {

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(clazz);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                new DefaultKafkaConsumerFactory<>(commonProps(), new StringDeserializer(), deserializer)
        );

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // NEW

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreateOrderCommand>
    sagaCreateOrderKafkaListenerFactory() {
        return factoryFor(CreateOrderCommand.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SagaPaymentCompletedEvent>
    sagaPaymentCompletedKafkaListenerFactory() {
        return factoryFor(SagaPaymentCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SagaNotificationSentEvent>
    sagaNotificationSentKafkaListenerFactory() {
        return factoryFor(SagaNotificationSentEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SagaOrderDeliveredEvent>
    sagaOrderDeliveredKafkaListenerFactory() {
        return factoryFor(SagaOrderDeliveredEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SagaInventoryReleasedEvent>
    sagaInventoryReleasedKafkaListenerFactory() {
        return factoryFor(SagaInventoryReleasedEvent.class);
    }
}