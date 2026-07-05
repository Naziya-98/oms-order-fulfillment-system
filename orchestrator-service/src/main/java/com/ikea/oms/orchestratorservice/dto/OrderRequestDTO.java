package com.ikea.oms.orchestratorservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "skuCode is mandatory")
    private String skuCode;

    @Min(value = 1, message = "Quantity should be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Customer Name is mandatory")
    private String customerName;

    @NotBlank(message = "Customer Email is mandatory")
    private String customerEmail;

    @NotBlank(message = "Shipping Address is mandatory")
    private String shippingAddress;
}
