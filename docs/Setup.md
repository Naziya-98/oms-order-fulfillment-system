# Setup

## Prerequisites

Before running the project, ensure the following software is installed.

- Java 17
- Maven
- PostgreSQL
- Apache Kafka
- Docker Desktop / Docker Engine (for Kong)

---

# Databases

Create the following PostgreSQL databases.

| Database |
|----------|
| orderdb |
| inventorydb |
| orchestratordb |

Payment Service and Notification Service are stateless and do not require a database.

---

# Start Order

Start the components in the following order.

1. PostgreSQL
2. Apache Kafka
3. Inventory Service
4. Order Service
5. Payment Service
6. Notification Service
7. Orchestrator Service (for Saga Orchestration)
8. Kong API Gateway

---

# Running the Services

Each service is an independent Spring Boot application.

Run each service using your preferred IDE or Maven.

Example:

```bash
mvn spring-boot:run
```

---

# Verifying the Setup

After all services are running:

- Swagger UI should be available for services where enabled.
- Kafka should be running.
- Databases should contain the required tables.
- Kong should proxy Order and Inventory services.

The APIs can be tested using Postman or curl.