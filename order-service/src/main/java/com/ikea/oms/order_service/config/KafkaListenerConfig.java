package com.ikea.oms.order_service.config;

import com.ikea.oms.order_service.event.NotificationSentEvent;
import com.ikea.oms.order_service.event.OrderDeliveredEvent;
import com.ikea.oms.order_service.event.PaymentCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.ikea.oms.order_service.event.InventoryReleasedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaListenerConfig {

    private Map<String, Object> commonProps() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-group-v2"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        return props;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
    paymentCompletedKafkaListenerFactory() {

        JsonDeserializer<PaymentCompletedEvent> deserializer =
                new JsonDeserializer<>(PaymentCompletedEvent.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationSentEvent>
    notificationKafkaListenerFactory() {

        JsonDeserializer<NotificationSentEvent> deserializer =
                new JsonDeserializer<>(NotificationSentEvent.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, NotificationSentEvent> factory =
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderDeliveredEvent>
    deliveredKafkaListenerFactory() {

        JsonDeserializer<OrderDeliveredEvent> deserializer =
                new JsonDeserializer<>(OrderDeliveredEvent.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, OrderDeliveredEvent> factory =
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InventoryReleasedEvent>
    inventoryReleasedKafkaListenerFactory() {

        JsonDeserializer<InventoryReleasedEvent> deserializer =
                new JsonDeserializer<>(InventoryReleasedEvent.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        ConcurrentKafkaListenerContainerFactory<String, InventoryReleasedEvent> factory =
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