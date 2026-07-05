package com.ikea.oms.orchestratorservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_orders")
@Data
public class SagaOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    private String skuCode;

    private Integer quantity;

    private BigDecimal unitPrice;

    private String customerName;

    private String customerEmail;

    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    // Human readable current step, useful for demo / interview walkthrough
    private String currentStep;

    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
