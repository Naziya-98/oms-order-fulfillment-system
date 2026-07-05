package com.ikea.oms.order_service.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SagaOrderDeliveredEvent {
    private Long orderId;
    private String orderNumber;
    private String deliveryStatus;
}