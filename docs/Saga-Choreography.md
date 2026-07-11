# Saga Choreography

## Overview

This document describes the Saga Choreography implementation used in the Order Management System (OMS) POC.

In this approach there is no central coordinator. Each service listens for Kafka events, performs its business logic, and publishes the next event in the workflow.

Each service only knows about the events it consumes and produces.

---

# Participating Services

- Order Service
- Inventory Service
- Payment Service
- Notification Service

---

# Workflow

## Successful Order Flow

1. The client creates an order by calling the Order Service.
2. Order Service validates and reserves inventory through Inventory Service using REST.
3. Order Service stores the order and publishes an order created event.
4. Payment Service consumes the event and processes payment.
5. Payment Service publishes a payment completed event.
6. Notification Service consumes the payment completed event and sends a notification.
7. Notification Service publishes notification sent and order delivered events.
8. Order Service updates the order status after receiving each event.

---

## Compensation Flow

If payment processing fails:

1. Payment Service publishes a payment failed event.
2. Inventory Service consumes the payment failed event.
3. Inventory Service releases the reserved stock.
4. Inventory Service publishes an inventory released event.
5. Order Service consumes the event and marks the order as CANCELLED.

---

# Event Flow

| Step | Producer | Kafka Topic | Consumer |
|------|----------|-------------|----------|
| 1 | Order Service | order-created-topic | Payment Service |
| 2 | Payment Service | payment-completed-topic | Order Service, Notification Service |
| 3 | Notification Service | notification-sent-topic | Order Service |
| 4 | Notification Service | order-delivered-topic | Order Service |
| 5 | Payment Service (Failure) | payment-failed-topic | Inventory Service |
| 6 | Inventory Service | inventory-released-topic | Order Service |

---

# Service Responsibilities

## Order Service

Produces:

- order-created-topic

Consumes:

- payment-completed-topic
- notification-sent-topic
- order-delivered-topic
- inventory-released-topic

Responsibilities:

- Create order
- Maintain order status
- Update order lifecycle

---

## Inventory Service

Consumes:

- payment-failed-topic

Produces:

- inventory-released-topic

Responsibilities:

- Reserve inventory
- Release inventory during compensation

---

## Payment Service

Consumes:

- order-created-topic

Produces:

- payment-completed-topic
- payment-failed-topic

Responsibilities:

- Process payment
- Publish payment outcome

---

## Notification Service

Consumes:

- payment-completed-topic

Produces:

- notification-sent-topic
- order-delivered-topic

Responsibilities:

- Send notification
- Simulate order delivery

---

# Order Status Progression

Successful flow:

```
CREATED
        ↓
INVENTORY_RESERVED
        ↓
PAYMENT_COMPLETED
        ↓
NOTIFICATION_SENT
        ↓
DELIVERED
```

Compensation flow:

```
CREATED
        ↓
INVENTORY_RESERVED
        ↓
PAYMENT_FAILED
        ↓
INVENTORY_RELEASED
        ↓
CANCELLED
```

---

# Sequence Diagram

Refer to:

```
docs/images/choreography_sequence.png
```