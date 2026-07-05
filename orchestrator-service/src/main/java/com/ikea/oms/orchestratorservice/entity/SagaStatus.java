package com.ikea.oms.orchestratorservice.entity;

public enum SagaStatus {
    STARTED,
    INVENTORY_RESERVED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    NOTIFICATION_PROCESSING,
    NOTIFICATION_SENT,
    COMPLETED,
    PAYMENT_FAILED,
    COMPENSATING,
    CANCELLED,
    FAILED
}
