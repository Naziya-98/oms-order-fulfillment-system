package com.ikea.oms.order_service.entity;

public enum OrderStatus {

    CREATED,

    INVENTORY_RESERVED,

    PAYMENT_PENDING,

    PAYMENT_COMPLETED,

    NOTIFICATION_SENT,

    DELIVERED,

    CANCELLED
}