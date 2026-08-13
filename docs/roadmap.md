# Delivery Roadmap

This roadmap grows the platform through small, demonstrable increments. Every sprint ends with working software, automated tests, updated documentation, and a scenario that can be shown during a technical interview.

Quality is part of every sprint. Testing, CI, documentation, and reproducible local execution are not postponed to a final phase.

## Release Status

| Version | Deliverable | Status |
|---|---|---|
| `v0.1.0` | Working payment terminal with persistence and Docker Compose | Released |
| `v0.2.0` | Sale authorization through a Java acquirer over TCP/IP | In progress |
| `v0.3.0` | Reliable transaction events and delayed retries | Planned |
| `v0.4.0` | SWIFT MT103 file import with Apache Camel | Planned |

## Planned Product Journey

1. A React POS terminal sends a sale to the Spring Boot transaction API.
2. The transaction service validates and persists the request.
3. A Java acquirer simulator returns an approved, declined, or unavailable result over TCP/IP.
4. Reliable events and retry jobs handle asynchronous follow-up work.
5. Apache Camel imports sample SWIFT MT103 files through a separate integration flow.

## Operation Scope

Payment operations do not all belong to the same lifecycle. The roadmap introduces them only when the platform has the rules and data required to represent them correctly.

| Operation | Meaning in this project | Planned scope |
|---|---|---|
| `SALE` | A purchase sent for authorization and completed as one simplified operation | Sprint 2 |
| Authorization | The processing step that decides whether a sale is approved or declined; it is not a separate POS transaction type | Sprint 2 |
| Preauthorization | A two-step flow that reserves funds before a later capture | Future increment |
| `CAPTURE` | Completes a previous preauthorization | Future increment |
| `VOID` | Cancels an authorization before capture or settlement | Future increment |
| `REFUND` | Creates a new financial transaction linked to an approved original transaction | Future increment |
| `REVERSAL` | Compensates an uncertain or failed technical outcome and references the original transaction | Sprint 3 |
| `SETTLEMENT` | Reconciles a batch of transactions; it belongs to a settlement process, not the POS transaction enum | Future increment |
| `CHARGEBACK` | Manages a dispute after processing; it belongs to a separate dispute model | Future increment |

The current `TransactionType` enum contains `SALE`, `REFUND`, and `REVERSAL`, but `v0.1.0` only provides generic capture and persistence with `PENDING` status. Refund and reversal business rules are not presented as complete. The enum remains suitable for a small, closed list of identifiers; new behavior should be added through operation-specific commands and handlers rather than entity inheritance.

## Sprint 1 - Payment Terminal MVP

**Status:** Complete

**Release:** `v0.1.0`

**Goal:** deliver a working local payment terminal that creates, stores, and retrieves transactions.

- [x] Create a Java 21 and Spring Boot transaction service using hexagonal architecture.
- [x] Create and retrieve transactions through a documented REST API.
- [x] Persist transactions with PostgreSQL and version the schema with Flyway.
- [x] Build a React and TypeScript POS terminal for transaction entry and lookup by UUID.
- [x] Map HTTP and persistence boundaries with MapStruct.
- [x] Define local, test, and production configuration profiles.
- [x] Verify domain, application, web, persistence, and HTTP behavior with automated tests.
- [x] Run PostgreSQL integration tests with Testcontainers.
- [x] Generate coverage reports with JaCoCo.
- [x] Verify backend and frontend changes with GitHub Actions.
- [x] Expose application health checks for local container orchestration.
- [x] Build backend and frontend images and run the complete platform with Docker Compose.
- [x] Document local setup, architecture, deployment, API usage, and the delivery roadmap.

**Demo:** start the platform with one Docker Compose command, create a transaction from the POS, persist it with `PENDING` status, and retrieve it by UUID.

## Sprint 2 - TCP/IP Acquirer Authorization

**Status:** In progress

**Target release:** `v0.2.0`

**Goal:** send a sale from the transaction service to a Java acquirer simulator over TCP/IP and return a clear authorization result to the POS.

The first increment defines the simplified ISO 8583 profile and implements an
interactive jPOS acquirer that lets an operator choose each response in the
console.

- [ ] Define an authorization port with provider-neutral request and response models.
- [ ] Create a standalone Java acquirer simulator with a TCP server.
- [ ] Implement a TCP client adapter in the transaction service.
- [ ] Define simplified ISO 8583 purchase messages with amount, STAN, RRN, terminal ID, and response code.
- [ ] Produce operator-controlled approved, declined, error, and timeout scenarios.
- [ ] Persist authorization metadata and the resulting transaction status.
- [ ] Add idempotency for repeated POS requests.
- [ ] Represent an unanswered request as an uncertain outcome without performing an unsafe automatic retry.
- [ ] Test authorization rules, message mapping, and TCP communication.
- [ ] Add the acquirer simulator to Docker Compose.

**Demo:** submit three sales from the POS and receive approved, declined, and timeout outcomes from the Java acquirer simulator.

## Sprint 3 - Reliable Events and Retries

**Status:** Planned

**Target release:** `v0.3.0`

**Goal:** publish transaction outcomes reliably and process delayed recovery work without risking duplicate financial operations.

- [ ] Store transaction events in a PostgreSQL outbox within the transaction commit.
- [ ] Publish created, approved, declined, and reversed events to Kafka.
- [ ] Use RabbitMQ for delayed recovery jobs after uncertain acquirer outcomes.
- [ ] Make event publication and retry consumers idempotent.
- [ ] Add dead-letter handling and observable failure states.
- [ ] Implement a controlled reversal flow linked to the original transaction.
- [ ] Verify PostgreSQL, Kafka, and RabbitMQ together with Testcontainers.
- [ ] Add correlation IDs and metrics for asynchronous processing.

**Demo:** authorize a transaction, inspect its Kafka event, and process a controlled recovery job without creating a duplicate charge.

## Sprint 4 - SWIFT File Import

**Status:** Planned

**Target release:** `v0.4.0`

**Goal:** import a sample SWIFT MT103 file through Apache Camel and turn valid records into traceable application commands.

- [ ] Define the supported MT103 fields and document the simplified assumptions.
- [ ] Create an Apache Camel route for file discovery, parsing, and validation.
- [ ] Transform valid records into provider-neutral application commands.
- [ ] Reject invalid files with clear error details and audit metadata.
- [ ] Prevent the same file from being processed twice.
- [ ] Add integration tests with valid, invalid, and duplicate sample files.
- [ ] Add health indicators and metrics for the import route.

**Demo:** import a valid sample file, inspect the resulting records, and show how an invalid or duplicate file is rejected.

## Future Improvements

- Add preauthorization, capture, void, refund, settlement, and chargeback workflows as separate domain increments.
- Publish coverage reports and verify container image builds in CI.
- Add SonarCloud quality analysis if it provides useful public evidence.
- Add a Jenkins pipeline only if a target role requires Jenkins experience.
- Add C4 diagrams, ADRs, and a public API collection as the architecture evolves.
- Add an optional fraud-scoring port with explicit data-protection boundaries.

## Definition of Done

A task is complete when its behavior is implemented, relevant tests pass, CI remains green, documentation reflects the change, and the pull request explains the delivered outcome.
