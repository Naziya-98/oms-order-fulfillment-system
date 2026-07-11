# Architecture

## Overview

This document provides an overview of the architecture of the Order Management System (OMS) Proof of Concept.

The project follows a microservices architecture where each service is responsible for a specific business capability. Services communicate using a combination of synchronous REST APIs and asynchronous messaging through Apache Kafka.

The order fulfillment workflow has been implemented using both Saga Choreography and Saga Orchestration. Both implementations coexist in the project and use separate Kafka topics so they can be executed independently.

---

# High-Level Architecture

The diagram below illustrates the overall architecture of the system.

> Insert `docs/images/overall_architecture.png`

The architecture consists of:

- Client applications (Postman / Swagger)
- Kong API Gateway
- Spring Boot microservices
- Apache Kafka
- PostgreSQL databases

---

# Microservices

| Service | Responsibility | Port |
|----------|----------------|------|
| Order Service | Creates customer orders and manages order status | 8082 |
| Inventory Service | Validates, reserves, and releases inventory | 8081 |
| Payment Service | Simulates payment processing | 8084 |
| Notification Service | Simulates customer notifications and delivery events | 8083 |
| Orchestrator Service | Coordinates the Saga Orchestration workflow | 8085 |

Each service is developed and deployed independently.

---

# Communication

The services communicate using two approaches.

### REST

REST APIs are used for synchronous operations that require an immediate response.

Current REST communication includes:

- Order Service → Inventory Service (choreography: validate + reserve inventory)
- Order Service → Inventory Service (orchestration: enrich product name only — a single `GET` call made when Order Service creates the order in response to `saga.create-order.command`)

The Orchestrator Service itself never makes a REST call to any participating service. Every
orchestration step (including inventory reservation) is a Kafka command or event — see
`Saga-Orchestration.md` for the full command/event table.

### Apache Kafka

Kafka is used for asynchronous communication between services.

Business events are published and consumed through Kafka, allowing services to remain loosely coupled.

Detailed topic information is available in `Kafka.md`.

---

# Database per Service

The project follows the Database per Service pattern.

| Service | Database |
|----------|----------|
| Order Service | orderdb |
| Inventory Service | inventorydb |
| Orchestrator Service | orchestratordb |
| Payment Service | No database |
| Notification Service | No database |

Each service owns and manages its own data.

---

# API Gateway

Kong API Gateway acts as the entry point for external requests.

The current implementation exposes:

- Order Service
- Inventory Service

Kong is configured with:

- JWT Authentication
- API Key Authentication
- Consumer configuration
- Rate Limiting
- Request Routing

Gateway configuration is documented in `Kong.md`.

---

# Related Documentation

Additional implementation details are available in the following documents.

| Document | Description |
|----------|-------------|
| Setup.md | Local development setup |
| Kafka.md | Kafka topics and event flow |
| Kong.md | Kong Gateway configuration |
| Saga-Choreography.md | Choreography workflow |
| Saga-Orchestration.md | Orchestration workflow |
| API.md | REST API reference |