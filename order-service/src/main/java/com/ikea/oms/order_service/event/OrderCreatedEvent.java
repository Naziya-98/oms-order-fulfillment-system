package com.ikea.oms.order_service.event;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderCreatedEvent {

        private Long orderId;
        private String orderNumber;
        private String skuCode;
        private Integer quantity;
    }

