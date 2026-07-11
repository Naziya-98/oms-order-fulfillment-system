package com.ikea.oms.order_service.dto;

import lombok.Data;

@Data
public class ReservationResponseDTO {
    private String reservationId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String status;
    private String message;
}
