# Platform Architecture

## Portfolio Scope

The platform simulates one clear financial journey: a POS terminal submits a payment, the transaction processor applies business rules, an acquirer returns an authorization decision, and the processor records the outcome.

The repository is delivered incrementally. Components shown as planned are not presented as current implementation.

## Target MVP

```mermaid
flowchart LR
    POS["POS Terminal\nSprint 1"] --> Processor["Transaction Processor\nHexagonal Architecture"]
    Processor --> Acquirer["Acquirer Simulator\nTCP/IP + ISO 8583\nSprint 2"]
    Processor --> Database[(PostgreSQL)]

    Processor -. "Sprint 3" .-> Events["Kafka Events"]
    Swift["SWIFT MT103 File\nSprint 3"] -.-> Camel["Apache Camel"] -.-> Processor
```

## Component Responsibilities

| Component | Responsibility | Delivery |
|---|---|---|
| POS Terminal | Spring MVC UI that submits a sale and shows its result | Sprint 1 |
| Transaction Processor | Domain rules, use cases, persistence ports, and HTTP API | Current foundation / Sprint 1 |
| PostgreSQL | Stores transactions and processing status | Current foundation |
| Acquirer Simulator | Deterministic authorization responses over TCP/IP using simplified ISO 8583 messages | Sprint 2 |
| Kafka | Publishes transaction lifecycle events through an outbox flow | Sprint 3 |
| Apache Camel | Imports sample SWIFT MT103 files into application commands | Sprint 3 |
| RabbitMQ | Schedules delayed retries for unavailable acquirers | Sprint 3 |

## Design Principles

| Principle | Application in this project |
|---|---|
| Hexagonal architecture | Domain and use cases depend on ports, never on HTTP, JPA, or messaging frameworks. |
| Incremental delivery | A diagram only treats a component as implemented once it has executable code and tests. |
| Financial traceability | Transactions use stable identifiers and record an explicit lifecycle status. |
| Resilience by use case | Timeouts and retries are introduced with the acquirer integration, not as speculative infrastructure. |
| Testability | Domain and application rules are verified independently from adapters. |

## Technology Evidence

- **Current foundation:** Java 21, Spring Boot, Spring MVC REST, PostgreSQL/JPA, Maven, Lombok, MapStruct, JUnit 5, Mockito, Testcontainers, and Docker Compose.
- **Sprint 2:** acquirer adapters, simplified ISO 8583 messages, retry, and circuit breaker behavior.
- **Sprint 3:** Kafka, RabbitMQ, Apache Camel, SWIFT import, and Java concurrency.
- **Sprint 4:** GitHub Actions, Jenkins, SonarQube, broader integration tests, and delivery documentation.
