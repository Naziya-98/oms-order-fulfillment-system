package com.ikea.oms.notificationservice.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Published to topic: saga.order-delivered.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaOrderDeliveredEvent {

    private Long orderId;
    private String orderNumber;
    private String deliveryStatus;
}
