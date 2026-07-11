package com.ikea.oms.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    // Correlates this row back to orchestrator-service's SagaOrder.id (the numeric
    // primary key of saga_orders, NOT the SAGA... orderNumber string). This is set
    // the instant the order is created here (same transaction as orderNumber), so
    // there is no async gap/race like there is with businessOrderNumber: every
    // saga.*.event the orchestrator forwards downstream already carries this same
    // numeric orderId from the very first command, so consumers here can always
    // find the right row immediately, even if payment or notification resolves
    // before this order row would otherwise be findable by business order number.
    private Long sagaOrderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String customerName;

    private String customerEmail;

    private String shippingAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems = new ArrayList<>();

}