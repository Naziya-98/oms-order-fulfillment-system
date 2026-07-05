package com.ikea.oms.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SKU purchased
    private String skuCode;

    // Product name at purchase time
    private String productName;

    // Price at purchase time
    private BigDecimal unitPrice;

    // Quantity purchased
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}