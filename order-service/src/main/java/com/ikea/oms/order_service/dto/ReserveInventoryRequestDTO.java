package com.ikea.oms.order_service.dto;

import lombok.Data;

@Data
public class ReserveInventoryRequestDTO {
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
}
