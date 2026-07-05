package com.ikea.oms.inventoryservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryRequestDTO {

    @NotBlank(message = "SKU Code is mandatory")
    private String skuCode;

    @NotBlank(message = "Product Name is mandatory")
    private String productName;

    @NotBlank(message = "Category is mandatory")
    private String category;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Unit Price should be greater than 0")
    private BigDecimal unitPrice;

    @Min(value = 1,
            message = "Quantity should be greater than 0")
    private Integer quantity;
}