package com.ikea.oms.notificationservice.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}

