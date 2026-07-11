package com.ikea.oms.orchestratorservice.entity;

public enum SagaStatus {
    STARTED,
    INVENTORY_RESERVATION_PENDING,
    INVENTORY_RESERVED,
    INVENTORY_RESERVATION_FAILED,
    PAYMENT_PROCESSING,
    PAYMENT_COMPLETED,
    NOTIFICATION_PROCESSING,
    NOTIFICATION_SENT,
    COMPLETED,
    PAYMENT_FAILED,
    COMPENSATING,
    CANCELLED,
    FAILED,

    // Set immediately when POST /{orderNumber}/cancel is called, before the
    // async compensation (release or refund-then-release) completes.
    CANCELLATION_REQUESTED,
    REFUND_PROCESSING
}
