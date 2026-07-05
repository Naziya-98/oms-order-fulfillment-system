package com.ikea.oms.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReleasedEvent {

    private Long orderId;

    private String orderNumber;

    private String inventoryStatus;

}