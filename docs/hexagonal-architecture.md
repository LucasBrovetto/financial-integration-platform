# Hexagonal Architecture

## Transaction Processor

The transaction processor keeps financial rules independent from Spring MVC, JPA, PostgreSQL, and future messaging providers. Inbound adapters call use cases; outbound adapters implement the ports required by those use cases.

```mermaid
flowchart LR
    Client["HTTP Client / POS"] --> Controller["REST Controller\nInbound Adapter"]
    Controller --> UseCase["Use Case\nInbound Port"]
    UseCase --> Service["Application Service"]
    Service --> Domain["Transaction Domain"]
    Service --> PersistencePort["Transaction Persistence Port"]
    PersistencePort --> JpaAdapter["JPA Adapter"]
    JpaAdapter --> Database[(PostgreSQL)]

    Service -. "Sprint 2" .-> AuthorizationPort["Authorization Port"]
    AuthorizationPort -. "Sprint 2" .-> Acquirer["Acquirer Adapter"]
    Service -. "Sprint 3" .-> EventPort["Event Port"]
    EventPort -. "Sprint 3" .-> Kafka["Kafka Adapter"]
```

## Current Package Structure

```text
com.financialintegration.transactionservice
├── domain/
│   ├── exception/
│   └── model/
├── application/
│   ├── exception/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── service/
├── adapters/
│   ├── inbound/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── mapper/
│   │   └── web/
│   └── outbound/
│       └── persistence/
└── TransactionServiceApplication.java
```

## Dependency Rules

1. The domain contains financial rules and has no Spring or persistence annotations.
2. The application layer owns use-case interfaces and outgoing ports.
3. Adapters translate between framework-specific models and domain or application models.
4. Future Kafka, ISO 8583, SWIFT, and AI integrations must be introduced as adapters behind explicit ports.

## Example: Create Transaction

```mermaid
sequenceDiagram
    participant POS as POS / HTTP Client
    participant Controller as Transaction Controller
    participant UseCase as Create Transaction Use Case
    participant Service as Application Service
    participant Domain as Transaction Domain
    participant Port as Persistence Port
    participant Adapter as JPA Adapter
    participant DB as PostgreSQL

    POS->>Controller: POST /transactions
    Controller->>UseCase: CreateTransactionCommand
    UseCase->>Service: execute(command)
    Service->>Domain: create and validate
    Service->>Port: save(transaction)
    Port->>Adapter: implement port
    Adapter->>DB: persist transaction
    DB-->>Adapter: saved entity
    Adapter-->>Service: transaction
    Service-->>Controller: transaction
    Controller-->>POS: 201 Created
```

## Testing Strategy

| Layer | Test focus | Tools |
|---|---|---|
| Domain | Lifecycle and validation rules | JUnit 5 |
| Application services | Port orchestration and error handling | JUnit 5 + Mockito |
| Web adapter | HTTP validation and error responses | Spring MVC test support |
| Persistence adapter | PostgreSQL mapping | Testcontainers |
