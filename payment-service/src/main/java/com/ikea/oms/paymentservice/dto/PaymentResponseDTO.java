package com.ikea.oms.paymentservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String status;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
