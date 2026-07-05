package com.ikea.oms.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique SKU of the product
    @Column(nullable = false, unique = true)
    private String skuCode;

    // Product display name
    @Column(nullable = false)
    private String productName;

    // Furniture, Electronics, etc.
    @Column(nullable = false)
    private String category;

    // Price of one unit
    @Column(nullable = false)
    private BigDecimal unitPrice;

    // Available stock
    @Column(nullable = false)
    private Integer quantity;
}