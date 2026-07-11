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

# Inventory Service — Routes and Endpoints

Inventory Service (`InventoryController`, base path `/api/inventory`) exposes five endpoints:

| Method | Path | Purpose |
|--------|------|---------|
| POST | /api/inventory | Create an inventory record |
| GET | /api/inventory | List all inventory |
| GET | /api/inventory/{skuCode} | Get inventory for one SKU |
| PATCH | /api/inventory/{skuCode}/{quantity} | Reserve/update inventory (used by the order flow) |
| PATCH | /api/inventory/stock/{skuCode}/{quantity} | Manual stock override (warehouse-only) |

Kong exposes two routes onto this same service:

- **`/inventory`** — general access. Reaches all five endpoints above. Requires a valid API key
  (`key-auth`) and is rate-limited to 6 requests/minute per consumer. No ACL restriction — any
  authenticated consumer (`customer-group` or `warehouse-group`) may call it.
- **`/inventory-admin`** — intended for the manual stock-override endpoint
  (`PATCH /api/inventory/stock/{sku}/{qty}`). Requires the same API key + rate limit as above,
  **plus** a route-scoped `acl` plugin restricting access to `warehouse-group` only.
  `customer-group` consumers receive `403 Forbidden` on this route.

Both routes forward to the same backend service with `strip_path: true`, so the distinction
between them is purely which Kong route (and therefore which plugin set) the caller goes through
— Kong does not restrict which downstream endpoint is reachable via which route, only which
consumers are authorized on it.

---

# Authentication

The current implementation demonstrates:

- JWT Authentication
- API Key Authentication

JWT is used for the Order Service.

API Key authentication is used for the Inventory Service.

---

# Authorization

Consumers are configured in Kong and assigned to ACL groups:

| Consumer | ACL Group | Credential |
|----------|-----------|------------|
| customer-api-client | customer-group | API key |
| ikea-client | customer-group | JWT |
| warehouse-client | warehouse-group | API key |

The `acl` plugin is attached at the **route** level, only on `/inventory-admin` — not at the
service level. This means it restricts only that route; `/inventory` remains open to any
authenticated consumer regardless of ACL group. This was a service-level plugin in an earlier
version of this configuration (which would have restricted `/inventory` as well); it has since
been moved to route-level scope specifically to allow general inventory access while keeping the
manual stock-override endpoint warehouse-only.

---

# Rate Limiting

Rate limiting is configured per consumer to demonstrate request throttling:

- Order Service (`/orders`): 5 requests/minute per consumer
- Inventory Service (`/inventory`, `/inventory-admin`): 6 requests/minute per consumer

---

# Request Flow

Client

↓

Kong

↓

Authentication (key-auth or JWT)

↓

Authorization (ACL, `/inventory-admin` only)

↓

Rate Limiting

↓

Routing

↓

Backend Service

A detailed request flow diagram is available in:

```
docs/images/kong_flow.png
```