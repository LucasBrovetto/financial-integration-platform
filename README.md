# Financial Integration Platform

[![Backend CI](https://github.com/LucasBrovetto/financial-integration-platform/actions/workflows/backend-ci.yml/badge.svg?branch=main)](https://github.com/LucasBrovetto/financial-integration-platform/actions/workflows/backend-ci.yml?query=branch%3Amain)
[![Frontend CI](https://github.com/LucasBrovetto/financial-integration-platform/actions/workflows/frontend-ci.yml/badge.svg?branch=main)](https://github.com/LucasBrovetto/financial-integration-platform/actions/workflows/frontend-ci.yml?query=branch%3Amain)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-React-3178C6?logo=typescript&logoColor=white)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A portfolio project that incrementally builds a payment flow from a point-of-sale terminal to an acquirer simulator. The current release demonstrates a working Java and Spring Boot service, React terminal, PostgreSQL persistence, automated testing, and reproducible local delivery.

## What Works Today

- React and TypeScript payment terminal
- Transaction creation and lookup by UUID
- Java 21 and Spring Boot transaction service
- Hexagonal architecture with ports and adapters
- PostgreSQL persistence and Flyway migrations
- OpenAPI documentation and Swagger UI
- Unit, web, persistence, and integration tests
- Backend and frontend verification with GitHub Actions
- Full local platform with Docker Compose

## Current Release Scope

`v0.1.0` is a local payment-processing simulation, not a production payment system. It accepts transaction data, applies basic validation, stores each transaction with `PENDING` status, and retrieves it by UUID through the POS terminal or REST API.

The release does not connect to a real acquirer, authorize cards, move funds, or store cardholder data. Although the domain and POS expose `SALE`, `REFUND`, and `REVERSAL` identifiers, operation-specific authorization, refund, and reversal rules are planned for later increments.

## Application Preview

![Financial Integration Platform POS terminal](docs/assets/pos-terminal.png)

The current release creates and retrieves pending transaction records. The operation selector previews the product direction, while authorization, refund, and reversal business flows are delivered in later increments described in the roadmap.

## Current Architecture

```mermaid
flowchart LR
    POS[React POS terminal] --> Processor[Transaction service]
    Processor --> Database[(PostgreSQL)]
```

This is the architecture implemented in `v0.1.0`. The acquirer simulator, messaging infrastructure, and file integrations described in the [delivery roadmap](docs/roadmap.md) are planned work and are shown separately in the [architecture documentation](docs/architecture.md).

## Roadmap Status

| Release | Deliverable | Status |
|---|---|---|
| `v0.1.0` | Payment terminal, REST API, persistence, tests, and Docker Compose | Released |
| `v0.2.0` | TCP/IP acquirer authorization with ISO 8583 and jPOS | In progress |
| `v0.3.0` | Reliable events and controlled recovery | Planned |
| `v0.4.0` | SWIFT MT103 file import | Planned |

See the [delivery roadmap](docs/roadmap.md) for sprint goals, demonstrations, and operation scope.

## Quick Start

This is the simplest way to review the complete application.

### Requirements

- Git
- Docker Desktop
- Ports `5173`, `8080`, and `5432` available

### Run the platform

```bash
git clone https://github.com/LucasBrovetto/financial-integration-platform.git
cd financial-integration-platform
docker compose up --build
```

Wait until the three services report a healthy status, then open:

- POS terminal: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- API health: `http://localhost:8080/actuator/health`

Stop the platform without deleting local transactions:

```bash
docker compose down
```

Delete the containers and PostgreSQL data when a clean database is required:

```bash
docker compose down --volumes
```

## Local Development

Use this workflow when running the backend from IntelliJ and the frontend from a terminal or VS Code.

### Requirements

- Java 21
- Node.js 24
- pnpm 11.9.0
- Docker Desktop

Copy the optional local configuration file from the repository root:

```bash
cp .env.example .env
```

Start the backend:

```bash
cd services/transaction-service
./mvnw spring-boot:run
```

The default `local` profile uses `docker-compose.dev.yml` to start PostgreSQL automatically. It does not start another backend or frontend container.

In a second terminal, install the frontend dependencies and start Vite:

```bash
cd apps/pos-terminal
pnpm install
pnpm dev
```

Do not run this development workflow and the full Docker Compose stack at the same time because they use the same ports.

When the backend is stopped, its managed PostgreSQL container remains available. Stop it from the repository root with:

```bash
docker compose -f docker-compose.dev.yml stop
```

## Quality Checks

Run backend unit and web tests:

```bash
cd services/transaction-service
./mvnw test
```

Run backend integration tests against PostgreSQL Testcontainers:

```bash
./mvnw verify -Pintegration
```

Run all frontend checks:

```bash
cd apps/pos-terminal
pnpm lint
pnpm test:run
pnpm build
```

## Configuration

- `local` is the default Spring profile and uses the PostgreSQL container started from `docker-compose.dev.yml`.
- `test` is used by Testcontainers and validates Flyway migrations against PostgreSQL.
- `prod` requires `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. It validates the schema and disables Swagger endpoints.

Database changes are versioned in `services/transaction-service/src/main/resources/db/migration`.

## API Documentation

With the backend running locally:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI document: `http://localhost:8080/v3/api-docs`

The OpenAPI document is the source of truth for consumers such as `pos-terminal`. The stable Sprint 1 operations are `createTransaction` and `getTransaction`.

## License

The original project code is licensed under the [MIT License](LICENSE). Sprint
2 introduces jPOS under GNU AGPLv3; see [third-party licenses](THIRD_PARTY_LICENSES.md)
for the obligations that apply to the combined jPOS-based application.
