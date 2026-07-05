package com.ikea.oms.order_service.dto;
import lombok.Data;
@Data
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String customerName;

    private String customerEmail;

    private String shippingAddress;

}
