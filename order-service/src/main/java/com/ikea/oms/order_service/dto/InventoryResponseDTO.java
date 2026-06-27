package com.ikea.oms.order_service.dto;
import lombok.Data;
@Data
public class InventoryResponseDTO {
    private Long id;
    private String skuCode;
    private Integer quantity;
}
