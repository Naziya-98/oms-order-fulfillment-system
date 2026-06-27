package com.ikea.oms.inventoryservice.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class InventoryRequestDTO {
    @NotBlank(message=" skuCode is Mandatory")
    private String skuCode;
    @Min(value=1 ,message ="Quantity should be greater than 0")
    private Integer quantity;
}
