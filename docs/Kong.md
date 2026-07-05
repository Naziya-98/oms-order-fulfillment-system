# Kong API Gateway

## Overview

Kong API Gateway acts as the entry point for external requests.

It provides centralized authentication, request routing, and rate limiting before requests reach the backend services.

---

# Services Exposed

The current configuration exposes:

- Order Service
- Inventory Service

The remaining services communicate internally.

---

# Authentication

The current implementation demonstrates:

- JWT Authentication
- API Key Authentication

JWT is used for the Order Service.

API Key authentication is used for the Inventory Service.

---

# Authorization

Consumers are configured in Kong and assigned to ACL groups.

Consumer groups are currently defined in the declarative configuration.

---

# Rate Limiting

Rate limiting is configured per consumer to demonstrate request throttling.

---

# Request Flow

Client

↓

Kong

↓

Authentication

↓

Routing

↓

Backend Service

A detailed request flow diagram is available in:

```
docs/images/kong-flow.png
```