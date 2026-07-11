# Saga Orchestration

## Overview

This document describes the Saga Orchestration implementation used in the Order Management System (OMS) POC.

Unlike Saga Choreography, this approach introduces a dedicated Orchestrator Service that coordinates the entire business workflow.

The Orchestrator maintains the current saga state and decides which command should be executed next.

**The entire orchestration workflow is Kafka-driven.** The Orchestrator never makes a direct REST call to any participating service — every step is a Kafka command or event. The one exception, noted below, is a single REST call made by *Order Service* (not the Orchestrator) purely to enrich a product name.

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

1. Client sends a request to Orchestrator Service. Orchestrator persists a `SagaOrder` row with status `STARTED`.
2. Orchestrator publishes `saga.reserve-inventory.command`.
3. Inventory Service checks and deducts stock, then publishes `saga.inventory-reserved.event`.
4. Orchestrator updates saga status to `INVENTORY_RESERVED`, then **immediately publishes two commands back-to-back** — `saga.create-order.command` and `saga.process-payment.command`. These are fired in parallel; the Orchestrator does **not** wait for Order Service to finish before triggering payment.
5. Order Service creates the order. While doing so it makes the one REST call in this entire flow — `GET /api/inventory/{sku}` on Inventory Service — solely to enrich the order line item with the product name. It then publishes `saga.order-created.event` back to the Orchestrator.
6. Payment Service processes payment and publishes `saga.payment-completed.event` (or `saga.payment-failed.event` — see Compensation Flow).
7. Orchestrator updates saga status to `PAYMENT_COMPLETED`, then publishes `saga.send-notification.command`.
8. Notification Service sends the notification, publishes `saga.notification-sent.event`, then publishes `saga.order-delivered.event`.
9. Orchestrator updates the saga status through `NOTIFICATION_SENT` to `COMPLETED`.

Note: Order Service also independently consumes `saga.payment-completed.event`, `saga.notification-sent.event`, `saga.order-delivered.event`, and `saga.inventory-released.event` (in the compensation flow) — this is a second, separate consumer group from the Orchestrator's, used only so Order Service can keep its own local `order.status` field current. The Orchestrator's `saga_orders` table remains the single source of truth for saga progress.

---

## Compensation Flow

If payment fails:

1. Payment Service publishes `saga.payment-failed.event`.
2. Orchestrator updates saga status to `PAYMENT_FAILED`, then `COMPENSATING`.
3. Orchestrator publishes `saga.release-inventory.command`.
4. Inventory Service releases the reserved inventory.
5. Inventory Service publishes `saga.inventory-released.event`.
6. Orchestrator marks the saga `CANCELLED`. Order Service (consuming the same event independently) marks its local order `CANCELLED` too.

---

# Kafka Commands and Events

| Step | Producer | Topic | Consumer |
|------|----------|-------|----------|
| 1 | Orchestrator | saga.reserve-inventory.command | Inventory Service |
| 2 | Inventory Service | saga.inventory-reserved.event | Orchestrator |
| 3 | Orchestrator | saga.create-order.command | Order Service |
| 4 | Orchestrator | saga.process-payment.command | Payment Service |
| 5 | Order Service | saga.order-created.event | Orchestrator |
| 6 | Payment Service | saga.payment-completed.event | Orchestrator, Order Service |
| 7 | Payment Service | saga.payment-failed.event | Orchestrator, Order Service |
| 8 | Orchestrator | saga.send-notification.command | Notification Service |
| 9 | Notification Service | saga.notification-sent.event | Orchestrator, Order Service |
| 10 | Notification Service | saga.order-delivered.event | Orchestrator, Order Service |
| 11 | Orchestrator | saga.release-inventory.command | Inventory Service |
| 12 | Inventory Service | saga.inventory-released.event | Orchestrator, Order Service |

Note: steps 3 and 4 are published back-to-back by the Orchestrator, not sequentially — see step 4 of the Successful Order Flow above.

---

# Service Responsibilities

## Orchestrator Service

Produces:

- saga.reserve-inventory.command
- saga.create-order.command
- saga.process-payment.command
- saga.send-notification.command
- saga.release-inventory.command

Consumes:

- saga.inventory-reserved.event
- saga.order-created.event
- saga.payment-completed.event
- saga.payment-failed.event
- saga.notification-sent.event
- saga.order-delivered.event
- saga.inventory-released.event

Responsibilities:

- Coordinate workflow
- Persist saga state (single source of truth — `saga_orders` table)
- Handle compensation
- Track current step

---

## Order Service

Consumes:

- saga.create-order.command
- saga.payment-completed.event (status sync only)
- saga.notification-sent.event (status sync only)
- saga.order-delivered.event (status sync only)
- saga.inventory-released.event (status sync only)

Produces:

- saga.order-created.event

Responsibilities:

- Create order record
- Make one REST call to Inventory Service (`GET /api/inventory/{sku}`) to enrich the product name
- Keep its own local `order.status` in sync by independently consuming the downstream saga events (separate consumer group from the Orchestrator's)

---

## Inventory Service

Consumes:

- saga.reserve-inventory.command
- saga.release-inventory.command

Produces:

- saga.inventory-reserved.event
- saga.inventory-released.event

Responsibilities:

- Reserve inventory (Kafka command — **not REST**)
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

**As of this revision, `orchestration-sequence.png` is being regenerated to match the Kafka-only
flow described above** — the previous version of this diagram showed inventory reservation as a
REST call, which does not match `SagaCommandProducer` / `SagaReserveInventoryCommandConsumer`.