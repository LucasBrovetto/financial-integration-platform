# Delivery Roadmap

This roadmap delivers a portfolio-grade financial integration platform in four one-week sprints. The scope prioritizes working software, automated verification, and clear technical evidence over a long list of unused technologies.

## Product Scope

The platform will simulate a point-of-sale transaction flow:

1. A Spring MVC POS terminal submits a payment request.
2. The transaction processor applies domain rules through hexagonal architecture.
3. A Visa or Mastercard simulator authorizes, declines, or times out.
4. The processor stores the result and publishes an event.
5. Apache Camel imports sample SWIFT files into the same processing flow.

## Sprint 1 - Foundations and POS MVC

**Goal:** run and verify a local transaction from a POS screen to PostgreSQL.

- [ ] Repair the Maven test suite for Java 21 and configure coverage reporting.
- [ ] Define local, test, and production configuration profiles.
- [ ] Introduce database migrations with Flyway.
- [ ] Create the `pos-terminal` Spring MVC module with a transaction form and result page.
- [ ] Complete transaction creation, retrieval, approval, decline, and idempotency use cases.
- [ ] Use MapStruct for persistence and HTTP boundary mappings.
- [ ] Build service images and run PostgreSQL, POS, and processor with Docker Compose.

**Demo:** submit a transaction from the POS UI and retrieve its persisted status.

## Sprint 2 - Acquirer Integration

**Goal:** authorize a transaction through deterministic Visa and Mastercard simulators.

- [ ] Define authorization ports and provider-neutral request and response models.
- [ ] Create a Visa acquirer adapter and simulator.
- [ ] Create a Mastercard acquirer adapter and simulator.
- [ ] Route authorizations by card metadata or test BIN ranges.
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
- [ ] Create a GitHub Actions pipeline for build, test, coverage, and image build.
- [ ] Add a declarative Jenkins pipeline.
- [ ] Integrate SonarQube or SonarCloud quality analysis.
- [ ] Document C4 diagrams, ADRs, API examples, ISO 8583, and SWIFT assumptions.
- [ ] Publish a Postman or Bruno collection and a short end-to-end demo recording.
- [ ] Add an optional `FraudScoringPort` with a deterministic fallback and explicit data-protection boundaries.

**Demo:** start the full stack with Docker Compose and run the documented end-to-end scenario.

## Definition of Done

A task is complete only when its acceptance criteria are met, tests pass, documentation is updated when behavior changes, and the pull request links to its issue.
