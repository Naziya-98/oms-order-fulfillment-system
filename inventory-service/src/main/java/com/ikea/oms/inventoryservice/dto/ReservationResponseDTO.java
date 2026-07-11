package com.ikea.oms.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponseDTO {

    private String reservationId;
    private String orderNumber;
    private String skuCode;
    private Integer quantity;
    private String status;   // "RESERVED" | "RELEASED"
    private String message;
}
