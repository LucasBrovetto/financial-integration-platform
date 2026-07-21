# Delivery Roadmap

This roadmap delivers a portfolio-grade financial integration platform in four one-week sprints. The scope prioritizes working software, automated verification, and clear technical evidence over a long list of unused technologies.

## Product Scope

The platform will simulate a point-of-sale transaction flow:

1. A Spring MVC POS terminal submits a payment request.
2. The transaction processor applies domain rules through hexagonal architecture.
3. An acquirer simulator authorizes, declines, or times out through TCP/IP using simplified ISO 8583 messages.
4. The processor stores the result and publishes an event.
5. Apache Camel imports sample SWIFT files into the same processing flow.

## Sprint 1 - Foundations and POS Terminal

**Goal:** run and verify a local transaction from a POS screen to PostgreSQL.

- [x] Repair the Maven test suite for Java 21 and configure coverage reporting.
- [x] Define local, test, and production configuration profiles.
- [x] Introduce database migrations with Flyway.
- [x] Document transaction endpoints with OpenAPI and verify the HTTP flow with PostgreSQL Testcontainers.
- [x] Create the `apps/pos-terminal` React module with a card-terminal experience, transaction result, and lookup by UUID.
- [x] Connect the POS terminal to the transaction API through the local Vite proxy.
- [x] Add frontend linting, interaction tests, API client tests, and a production build.
- [x] Complete transaction creation and retrieval use cases.
- [ ] Complete approval, decline, and idempotency use cases.
- [x] Use MapStruct for HTTP boundary mappings.
- [x] Introduce a MapStruct persistence mapper for domain and entity transformations.
- [x] Run local PostgreSQL with Docker Compose.
- [x] Build service images and run PostgreSQL, POS, and processor with Docker Compose.

**Demo:** submit a transaction from the POS UI and retrieve its persisted status.

## Sprint 2 - Acquirer Integration

**Goal:** authorize a transaction through a deterministic acquirer simulator over TCP/IP.

- [ ] Define authorization ports and provider-neutral request and response models.
- [ ] Create a TCP/IP acquirer simulator with deterministic authorization scenarios.
- [ ] Create a TCP/IP acquirer adapter behind the authorization port.
- [ ] Route authorizations by card metadata or explicit test scenarios.
- [ ] Model a simplified ISO 8583 request and response including STAN, RRN, and response code.
- [ ] Add timeout, retry, circuit-breaker, and idempotency handling.
- [ ] Cover authorization rules and adapters with JUnit 5 and Mockito tests.

**Demo:** process approved, declined, and unavailable acquirer scenarios from the POS UI.

## Sprint 3 - Event-Driven Integrations

**Goal:** publish reliable transaction events and import financial files.

- [ ] Add Kafka topics for created, approved, declined, and reversed transactions.
- [ ] Implement an outbox-based event publishing flow.
- [ ] Add RabbitMQ for delayed authorization retry jobs.
- [ ] Create Apache Camel routes to import sample SWIFT MT103 files.
- [ ] Transform imported records into application commands with validation and audit metadata.
- [ ] Use Java 21 virtual threads or `CompletableFuture` for a documented integration workload.
- [ ] Add correlation IDs, Actuator health checks, and transaction metrics.

**Demo:** import a sample file and inspect its resulting transaction and Kafka event.

## Sprint 4 - Quality and Delivery

**Goal:** publish a reproducible, well-documented portfolio project.

- [ ] Add Testcontainers integration tests for PostgreSQL, Kafka, and RabbitMQ.
- [x] Add baseline backend verification with GitHub Actions.
- [ ] Extend GitHub Actions with frontend verification, coverage publication, and image builds.
- [ ] Add a declarative Jenkins pipeline.
- [ ] Integrate SonarQube or SonarCloud quality analysis.
- [ ] Document C4 diagrams, ADRs, API examples, ISO 8583, and SWIFT assumptions.
- [ ] Publish a Postman or Bruno collection and a short end-to-end demo recording.
- [ ] Add an optional `FraudScoringPort` with a deterministic fallback and explicit data-protection boundaries.

**Demo:** start the full stack with Docker Compose and run the documented end-to-end scenario.

## Definition of Done

A task is complete only when its acceptance criteria are met, tests pass, documentation is updated when behavior changes, and the pull request links to its issue.
