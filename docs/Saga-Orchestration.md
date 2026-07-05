# Saga Orchestration

## Overview

This document describes the Saga Orchestration implementation used in the Order Management System (OMS) POC.

Unlike Saga Choreography, this approach introduces a dedicated Orchestrator Service that coordinates the entire business workflow.

The Orchestrator maintains the current saga state and decides which command should be executed next.

---

# Participating Services

- Orchestrator Service
- Order Service
- Inventory Service
- Payment Service
- Notification Service

---

# Workflow

## Successful Order Flow

1. Client sends a request to Orchestrator Service.
2. Orchestrator reserves inventory using REST.
3. Orchestrator stores saga information.
4. Orchestrator publishes a command to create the order.
5. Orchestrator publishes a payment command.
6. Payment Service processes payment.
7. Payment Service publishes a payment completed event.
8. Orchestrator publishes a notification command.
9. Notification Service sends notification.
10. Notification Service publishes notification sent and order delivered events.
11. Orchestrator updates the saga status until completion.

---

## Compensation Flow

If payment fails:

1. Payment Service publishes a payment failed event.
2. Orchestrator updates saga status.
3. Orchestrator publishes an inventory release command.
4. Inventory Service releases reserved inventory.
5. Inventory Service publishes inventory released event.
6. Orchestrator marks the saga as CANCELLED.

---

# Kafka Commands and Events

| Step | Producer | Topic | Consumer |
|------|----------|-------|----------|
| 1 | Orchestrator | saga.create-order.command | Order Service |
| 2 | Orchestrator | saga.process-payment.command | Payment Service |
| 3 | Payment Service | saga.payment-completed.event | Orchestrator |
| 4 | Payment Service | saga.payment-failed.event | Orchestrator |
| 5 | Orchestrator | saga.send-notification.command | Notification Service |
| 6 | Notification Service | saga.notification-sent.event | Orchestrator |
| 7 | Notification Service | saga.order-delivered.event | Orchestrator |
| 8 | Orchestrator | saga.release-inventory.command | Inventory Service |
| 9 | Inventory Service | saga.inventory-released.event | Orchestrator |

---

# Service Responsibilities

## Orchestrator Service

Produces:

- saga.create-order.command
- saga.process-payment.command
- saga.send-notification.command
- saga.release-inventory.command

Consumes:

- saga.payment-completed.event
- saga.payment-failed.event
- saga.notification-sent.event
- saga.order-delivered.event
- saga.inventory-released.event

Responsibilities:

- Coordinate workflow
- Persist saga state
- Handle compensation
- Track current step

---

## Order Service

Consumes:

- saga.create-order.command

Responsibilities:

- Create order record

---

## Inventory Service

Consumes:

- saga.release-inventory.command

Produces:

- saga.inventory-released.event

Responsibilities:

- Reserve inventory (REST)
- Release inventory during compensation

---

## Payment Service

Consumes:

- saga.process-payment.command

Produces:

- saga.payment-completed.event
- saga.payment-failed.event

Responsibilities:

- Process payment
- Publish payment result

---

## Notification Service

Consumes:

- saga.send-notification.command

Produces:

- saga.notification-sent.event
- saga.order-delivered.event

Responsibilities:

- Send notification
- Publish delivery event

---

# Saga Status Progression

Successful flow:

```
STARTED
        ↓
INVENTORY_RESERVED
        ↓
PAYMENT_PROCESSING
        ↓
PAYMENT_COMPLETED
        ↓
NOTIFICATION_PROCESSING
        ↓
NOTIFICATION_SENT
        ↓
COMPLETED
```

Compensation flow:

```
STARTED
        ↓
INVENTORY_RESERVED
        ↓
PAYMENT_PROCESSING
        ↓
PAYMENT_FAILED
        ↓
COMPENSATING
        ↓
CANCELLED
```

---

# Sequence Diagram

Refer to:

```
docs/images/orchestration-sequence.png
```