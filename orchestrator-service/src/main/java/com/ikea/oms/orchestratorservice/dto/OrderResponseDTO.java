package com.ikea.oms.orchestratorservice.dto;

import com.ikea.oms.orchestratorservice.entity.SagaStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private String businessOrderNumber;
    private String skuCode;
    private Integer quantity;
    private BigDecimal unitPrice;

    private String customerName;
    private String customerEmail;
    private String shippingAddress;

    private SagaStatus status;
    private String currentStep;
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
