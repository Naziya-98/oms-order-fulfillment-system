package com.ikea.oms.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReserveInventoryRequestDTO {

    @NotBlank
    private String orderNumber;

    @NotBlank
    private String skuCode;

    @NotNull
    @Min(1)
    private Integer quantity;
}
