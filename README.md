# Order Management API

A personal portfolio project for Nivas Pari demonstrating Java backend development with Spring Boot, relational persistence, validation, automated testing, and container packaging. Created as a new portfolio demonstration; it is not an employer or client project and does not claim production usage.

## Features

- Create, retrieve, and paginate single-product orders.
- Compute monetary totals with `BigDecimal`.
- Enforce an explicit order lifecycle: `PENDING -> CONFIRMED -> SHIPPED`; pending or confirmed orders can be cancelled. Shipped and cancelled orders are terminal.
- Validate request fields and return structured HTTP problem responses.
- Persist orders with JPA, PostgreSQL, Flyway migrations, and optimistic locking to detect overlapping updates.
- Run JUnit API integration tests and Mockito service tests. GitHub Actions runs Maven verification.

## Architecture

HTTP controller / validated DTOs -> transactional service -> JPA repository -> PostgreSQL.

The API returns dedicated response records rather than exposing entities. Database constraints complement request validation. Lists use bounded pagination and stable creation-time/ID ordering. Flyway owns the schema; Hibernate validates it at startup.

## Run with Docker

1. Copy `.env.example` to `.env` and replace the sample password.
2. Run `docker compose up --build`.
3. Check `http://localhost:8080/actuator/health`.

The API is bound to localhost; the database is not exposed on a host port. `docker compose down` stops containers; add `-v` only if you intend to delete the development database.

## Run locally

Requires Java 17+ and Maven 3.6.3+. Supply a PostgreSQL database through `DB_URL`, `DB_USER`, and `DB_PASSWORD`, then run `mvn spring-boot:run`.

Run `mvn verify` for tests and the executable JAR. Tests use H2 in PostgreSQL compatibility mode and execute the Flyway migration. They do not require Docker. H2 testing does not prove complete PostgreSQL compatibility; validate the Docker setup against PostgreSQL before deploying.

## API

| Method | Path | Behavior |
| --- | --- | --- |
| POST | `/api/orders` | Create an order; returns 201 and Location |
| GET | `/api/orders/{id}` | Retrieve an order, or 404 |
| GET | `/api/orders?page=0&size=20` | Paginate orders; size 1–100 |
| PATCH | `/api/orders/{id}/status` | Apply valid status transition, or 409 |
| GET | `/actuator/health` | Health check |

Create an order:

```bash
curl -i http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customer":"Sample Customer","product":"Keyboard","quantity":3,"unitPrice":19.99}'
```

Use the returned ID:

```bash
curl -X PATCH http://localhost:8080/api/orders/ORDER_ID/status \
  -H 'Content-Type: application/json' -d '{"status":"CONFIRMED"}'
```

A cancelled or shipped order cannot be changed. Repeating the same transition returns 409. A missing order returns 404; malformed or invalid input returns 400.

## Scope and tradeoffs

This is one backend service, not a distributed microservices system. Prices are supplied by the caller for demonstration; there is no product catalogue, currency conversion, stock reservation, payment processing, or customer authentication. The application has no authorization layer and must not be exposed as a production ordering system without adding access controls, trusted pricing, operational monitoring, and deployment hardening. Creation is not idempotent; retrying a POST can create another order.

## Interview walkthrough

1. Explain the controller/service/repository boundaries and transaction scope.
2. Demonstrate invalid input, allowed transitions, and a rejected transition.
3. Discuss why decimal money and optimistic locking matter.
4. Walk through the API and service tests, and the limits of H2 compatibility testing.
5. Outline how you would add authentication, idempotency keys, inventory, and event delivery.
