# API Reference

## Overview

This document provides a high-level reference for the REST APIs exposed by each service in the Order Management System (OMS) Proof of Concept.

The project exposes the Order Service and Inventory Service through Kong API Gateway. The remaining services are intended for internal communication and are accessed directly during development or testing.

---

# Gateway Endpoints (Kong)

The following APIs are available through Kong API Gateway.

Gateway Base URL

```
http://localhost:8000
```

| Method | Gateway Endpoint | Backend Service | Authentication |
|---------|------------------|-----------------|----------------|
| POST | `/orders/api/orders` | Order Service | JWT |
| GET | `/orders/api/orders` | Order Service | JWT |
| GET | `/orders/api/orders/{id}` | Order Service | JWT |
| GET | `/inventory/api/inventory/{skuCode}` | Inventory Service | API Key |
| PATCH | `/inventory/api/inventory/{skuCode}/{quantity}` | Inventory Service | API Key |

Kong currently provides:

- JWT Authentication
- API Key Authentication
- Consumer Management
- ACL Group Configuration
- Rate Limiting
- Request Routing
- Request Logging

---

# Order Service

Internal Base URL

```
http://localhost:8082
```

Gateway Base URL

```
http://localhost:8000/orders
```

### Create Order

| Method | Endpoint |
|---------|----------|
| POST | `/api/orders` |

Purpose

Creates a new customer order and starts the Saga Choreography workflow.

---

### Get All Orders

| Method | Endpoint |
|---------|----------|
| GET | `/api/orders` |

Purpose

Returns all orders stored in the Order Service.

---

### Get Order By Id

| Method | Endpoint |
|---------|----------|
| GET | `/api/orders/{id}` |

Purpose

Returns a specific order.

---

# Inventory Service

Internal Base URL

```
http://localhost:8081
```

Gateway Base URL

```
http://localhost:8000/inventory
```

### Check Inventory

| Method | Endpoint |
|---------|----------|
| GET | `/api/inventory/{skuCode}` |

Purpose

Returns inventory information for a SKU.

---

### Reserve Inventory

| Method | Endpoint |
|---------|----------|
| PATCH | `/api/inventory/{skuCode}/{quantity}` |

Purpose

Reserves inventory for an order.

---

# Orchestrator Service

Internal Base URL

```
http://localhost:8085
```

### Create Orchestrated Order

| Method | Endpoint |
|---------|----------|
| POST | `/api/orchestrated-orders` |

Purpose

Starts the Saga Orchestration workflow.

---

### Get Saga Status

| Method | Endpoint |
|---------|----------|
| GET | `/api/orchestrated-orders/{orderNumber}` |

Purpose

Returns the current Saga status for an orchestrated order.

---

# Payment Service

Internal Base URL

```
http://localhost:8084
```

The Payment Service does not expose REST APIs for external clients.

It processes Kafka events published during Saga Choreography and Saga Orchestration.

---

# Notification Service

Internal Base URL

```
http://localhost:8083
```

The Notification Service does not expose REST APIs for external clients.

It consumes Kafka events and publishes notification and delivery events.

---

# Authentication

The current implementation supports the following authentication mechanisms.

## JWT Authentication

Used for requests routed to the Order Service through Kong.

The repository contains the `security-utils` project, which generates JWT tokens for testing protected APIs.

---

## API Key Authentication

Used for requests routed to the Inventory Service through Kong.

Clients must provide the configured API Key when accessing Inventory APIs through Kong.

---

# API Testing

The APIs can be tested using:

- Postman
- curl
- Swagger UI (where enabled)

For demonstrations, requests are typically sent through Kong Gateway so that authentication, authorization, routing, and rate limiting can be observed.

---

# Related Documentation

- Architecture.md
- Setup.md
- Kafka.md
- Saga-Choreography.md
- Saga-Orchestration.md
- Kong.md