package com.ikea.oms.orchestratorservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Consumed from topic: saga.order-delivered.event
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeliveredEvent {

    private Long orderId;
    private String orderNumber;
    private String deliveryStatus;
}
