# Order Management System (OMS) – Proof of Concept

## Overview

This repository contains a backend proof of concept for an Order Management System (OMS) built using Java 17 and Spring Boot.

The primary objective of this project was to understand and implement the order fulfillment workflow commonly used in modern e-commerce applications. The implementation follows a microservices architecture where independent services collaborate to complete an order lifecycle while remaining independently deployable.

The project demonstrates how an order moves through inventory reservation, payment processing, customer notification, and delivery using asynchronous communication with Apache Kafka. To compare different implementation approaches, the same business workflow has been implemented using both Saga Choreography and Saga Orchestration patterns.

Kong API Gateway is used to expose selected services through a single entry point and demonstrate authentication, authorization, request routing, and rate limiting.

This is a backend-only proof of concept. There is no frontend application. APIs are tested using Postman or curl, and the overall workflow can be verified through application logs, Kafka events, and the underlying databases.

---

# Project Objectives

The project was built to gain hands-on experience with backend microservices by implementing an end-to-end Order Management System.

The implementation focuses on:

- Designing independently deployable Spring Boot services
- Understanding the order fulfillment workflow
- Event-driven communication using Apache Kafka
- Implementing Saga Choreography
- Implementing Saga Orchestration
- Applying the Database per Service pattern
- REST-based service communication
- Kong API Gateway integration
- JWT Authentication
- API Key Authentication
- ACL-based authorization
- Rate limiting

---

# Technology Stack

| Area | Technology |
|------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build Tool | Maven |
| Database | PostgreSQL |
| Messaging | Apache Kafka |
| API Gateway | Kong 3.x |
| API Documentation | Swagger (springdoc-openapi) |
| Authentication | JWT |
| Authorization | Kong ACL |
| Communication | REST + Kafka |

---

# Repository Structure

```
order-service/
inventory-service/
payment-service/
notification-service/
orchestrator-service/
security-utils/
kong/
docs/
```

## Service Overview

| Module | Responsibility |
|----------|----------------|
| order-service | Creates customer orders and manages order state |
| inventory-service | Validates, reserves, and releases inventory |
| payment-service | Simulates payment processing |
| notification-service | Simulates customer notification and delivery events |
| orchestrator-service | Coordinates the Saga Orchestration workflow |
| security-utils | Utility project for generating JWT tokens used during Kong authentication testing |
| kong | Kong Gateway configuration and Docker Compose files |
| docs | Project documentation |

Each service is an independent Spring Boot application with its own Maven configuration. Services can be built, executed, and maintained independently.

---

# Architecture Overview

The project implements the same business workflow using two different Saga patterns.

## Saga Choreography

The choreography implementation allows services to communicate directly through Kafka events without a central coordinator.

Participating services:

- Order Service
- Inventory Service
- Payment Service
- Notification Service

## Saga Orchestration

The orchestration implementation introduces a dedicated Orchestrator Service that coordinates the complete workflow while communicating with the remaining services through REST and Kafka.

Participating services:

- Orchestrator Service
- Order Service
- Inventory Service
- Payment Service
- Notification Service

Both implementations use separate Kafka topics, allowing either workflow to be executed independently without affecting the other.

The Inventory Service and Order Service are currently exposed through Kong API Gateway. The remaining services communicate internally and are not exposed through the gateway.

Architecture diagrams and sequence diagrams are maintained under the `docs/images` directory and are referenced from the corresponding documentation pages.
---

# Database Design

The project follows the Database per Service pattern.

| Service | Database |
|----------|----------|
| Order Service | orderdb |
| Inventory Service | inventorydb |
| Orchestrator Service | orchestratordb |
| Payment Service | No database (stateless) |
| Notification Service | No database (stateless) |

---

# Security

Kong API Gateway is used as the entry point for external requests.

The current implementation includes:

- JWT Authentication
- API Key Authentication
- Consumer configuration
- ACL-based authorization
- Rate limiting
- Request routing
- Request logging

The `security-utils` project contains a standalone utility that generates JWT tokens for testing secured endpoints exposed through Kong.

---

# Documentation

Additional documentation is available under the `docs` directory.

| Document | Description |
|----------|-------------|
| Architecture.md | Overall system architecture and deployment |
| Setup.md | Local development environment setup |
| Kong.md | Kong Gateway configuration |
| Kafka.md | Kafka topics, producers, and consumers |
| Saga-Choreography.md | Choreography workflow |
| Saga-Orchestration.md | Orchestration workflow |
| API.md | REST API reference |
| DeveloperNotes.md | Development notes and implementation details |
| Roadmap.md | Planned enhancements |

The README provides a high-level overview of the project. Detailed implementation notes, architecture diagrams, sequence diagrams, setup instructions, and API documentation are maintained in the `docs` folder.

---

# Getting Started

Detailed setup instructions are available in:

```
docs/Setup.md
```

At a high level:

1. Start PostgreSQL.
2. Create the required databases.
3. Start Apache Kafka.
4. Start the Spring Boot services.
5. Start Kong Gateway using Docker Compose.
6. Test the APIs using Postman or curl.

---

# Current Status

The project has been developed and tested locally.

The current implementation includes:

- Working Saga Choreography workflow
- Working Saga Orchestration workflow
- Kafka-based asynchronous communication
- Database per Service architecture
- Kong API Gateway integration
- JWT Authentication
- API Key Authentication
- ACL-based authorization
- Rate limiting
- Swagger API documentation

The project is intended as a learning and demonstration proof of concept for backend microservices and the order fulfillment workflow.

