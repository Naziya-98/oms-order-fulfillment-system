package com.ikea.oms.order_service.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class OrderRequestDTO {

    @NotBlank(message ="skuCode is mandatory")
    private String skuCode;

    @Min(value=1 ,  message =" quantity should be greater than 0")
    private Integer quantity;
}
