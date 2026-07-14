# Transaction Flows

## Current Foundation: Create a Pending Transaction

```mermaid
sequenceDiagram
    participant Client as HTTP Client
    participant API as Transaction API
    participant Service as Create Transaction Service
    participant Domain as Transaction Domain
    participant DB as PostgreSQL

    Client->>API: POST /transactions
    API->>Service: CreateTransactionCommand
    Service->>Domain: validate and create
    Domain-->>Service: PENDING transaction
    Service->>DB: persist transaction
    DB-->>Service: saved transaction
    Service-->>API: transaction
    API-->>Client: 201 Created
```

## Sprint 2: Acquirer Authorization

```mermaid
sequenceDiagram
    participant POS as POS Terminal
    participant Processor as Transaction Processor
    participant Acquirer as Visa / Mastercard Simulator
    participant DB as PostgreSQL

    POS->>Processor: sale request
    Processor->>Acquirer: simplified ISO 8583 authorization
    Acquirer-->>Processor: approved or declined
    Processor->>DB: store final status
    Processor-->>POS: transaction result
```

## Sprint 2: Acquirer Timeout

```mermaid
sequenceDiagram
    participant POS as POS Terminal
    participant Processor as Transaction Processor
    participant Acquirer as Acquirer Simulator
    participant DB as PostgreSQL

    POS->>Processor: sale request
    Processor->>Acquirer: authorization request
    Acquirer--xProcessor: timeout
    Processor->>DB: store PENDING status
    Processor-->>POS: processing delayed
    Note over Processor,Acquirer: Retry policy is introduced in Sprint 2.
```

## Sprint 3: Event Publication

After an outbox record is stored with the transaction, an event publisher sends lifecycle events to Kafka. This is intentionally planned work, not part of the current transaction-service foundation.
