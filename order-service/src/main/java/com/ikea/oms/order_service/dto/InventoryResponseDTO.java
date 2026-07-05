package com.ikea.oms.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryResponseDTO {

    private Long id;

    private String skuCode;

    private String productName;

    private String category;

    private BigDecimal unitPrice;

    private Integer quantity;
}