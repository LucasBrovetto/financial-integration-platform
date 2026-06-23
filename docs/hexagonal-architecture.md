# Hexagonal Architecture (Transaction Service)

## Internal Structure - Ports & Adapters Pattern

```mermaid
graph TB
    subgraph "Inbound Adapters (→ Application)"
        REST["🌐 REST Controller<br/>@RestController"]
        DTO["📦 DTOs<br/>Request/Response Records"]
        MAPPER["🔄 Mapper<br/>DTO ↔ Domain"]
    end

    subgraph "Application Layer"
        USECASE["🎯 Use Cases (Ports)<br/>CreateTransactionUseCase<br/>GetTransactionUseCase"]
        SERVICE["⚙️ Application Services<br/>CreateTransactionService<br/>GetTransactionService"]
    end

    subgraph "Domain Layer (Pure)"
        DOMAIN["🏛️ Domain Models<br/>(Entities, Value Objects)<br/>No Spring Annotations<br/>No JPA Annotations<br/>No External Dependencies"]
    end

    subgraph "Outbound Adapters (← Application)"
        PERSIST_PORT["💾 Persistence Port<br/>TransactionPersistencePort"]
        JPA["🗄️ JPA Adapter<br/>TransactionJpaAdapter"]
        ENTITY["📊 JPA Entity<br/>TransactionEntity"]
        JPAREPOSITORY["🔍 Spring Data JPA<br/>TransactionJpaRepository"]
        
        KAFKA_PORT["📨 Kafka Port<br/>TransactionEventPort"]
        KAFKA_ADAPTER["📤 Kafka Adapter<br/>TransactionEventAdapter"]
        KAFKA_PRODUCER["📡 Kafka Producer<br/>Event Publisher"]
    end

    subgraph "Infrastructure"
        DB[("🗄️ PostgreSQL")]
        KAFKA_BROKER[("📨 Kafka Broker")]
    end

    %% Inbound Flow
    REST -->|Receives Request| DTO
    DTO -->|Maps| MAPPER
    MAPPER -->|Calls| USECASE
    USECASE -->|Implemented by| SERVICE

    %% Domain Flow
    SERVICE -->|Uses| DOMAIN
    SERVICE -->|Calls Port| PERSIST_PORT
    SERVICE -->|Calls Port| KAFKA_PORT

    %% Outbound Persistence Flow
    PERSIST_PORT -->|Implemented by| JPA
    JPA -->|Uses| JPAREPOSITORY
    JPAREPOSITORY -->|Maps Entity| ENTITY
    ENTITY -->|Persists to| DB

    %% Outbound Kafka Flow
    KAFKA_PORT -->|Implemented by| KAFKA_ADAPTER
    KAFKA_ADAPTER -->|Publishes| KAFKA_PRODUCER
    KAFKA_PRODUCER -->|Sends to| KAFKA_BROKER

    %% Response Flow (return)
    SERVICE -.->|Returns Domain| MAPPER
    MAPPER -.->|Converts to| DTO
    DTO -.->|Sends Response| REST

    style REST fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style DTO fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style MAPPER fill:#4A90E2,stroke:#2E5C8A,color:#fff
    
    style USECASE fill:#F5A623,stroke:#C17E1B,color:#fff
    style SERVICE fill:#F5A623,stroke:#C17E1B,color:#fff
    
    style DOMAIN fill:#7ED321,stroke:#5FA119,color:#000
    
    style PERSIST_PORT fill:#BD10E0,stroke:#8B0AA8,color:#fff
    style JPA fill:#BD10E0,stroke:#8B0AA8,color:#fff
    style ENTITY fill:#BD10E0,stroke:#8B0AA8,color:#fff
    style JPAREPOSITORY fill:#BD10E0,stroke:#8B0AA8,color:#fff
    
    style KAFKA_PORT fill:#50E3C2,stroke:#2BA39A,color:#000
    style KAFKA_ADAPTER fill:#50E3C2,stroke:#2BA39A,color:#000
    style KAFKA_PRODUCER fill:#50E3C2,stroke:#2BA39A,color:#000
    
    style DB fill:#D0021B,stroke:#8B0000,color:#fff
    style KAFKA_BROKER fill:#D0021B,stroke:#8B0000,color:#fff
```

---

## Package Structure

```
com.financialintegration.transactionservice
│
├── domain/                              # 🏛️ Pure Domain (no Spring, no JPA)
│   ├── model/                           # Domain entities & value objects
│   └── exception/                       # Domain-specific exceptions
│
├── application/                         # 🎯 Application Layer
│   ├── port/
│   │   ├── in/                          # 📥 Input Ports (Use Cases)
│   │   └── out/                         # 📤 Output Ports
│   └── service/                         # ⚙️ Application Services
│
├── adapters/
│   ├── inbound/                         # 🌐 Inbound Adapters
│   │   ├── web/                         # REST Controller
│   │   └── dto/                         # DTOs & Mappers
│   └── outbound/                        # 💾 Outbound Adapters
│       ├── persistence/                 # JPA Adapter & Repository
│       └── kafka/                       # Kafka Event Publisher
│
├── config/                              # ⚙️ Spring Configuration & Beans
├── exception/                           # 🚨 Global Exception Handler
└── TransactionServiceApplication.java   # 🚀 Bootstrap Class
```

---

## Dependency Flow

```mermaid
graph LR
    A["🌐 Inbound<br/>Controllers"] --> B["🎯 Use Cases<br/>Ports"]
    C["🏛️ Domain<br/>Pure Logic"] --> B
    B --> D["📤 Output<br/>Ports"]
    D --> E["💾 Outbound<br/>Adapters"]
    
    F["🌐 Request"] --> A
    E --> G["🗄️ External Systems<br/>DB / Kafka / APIs"]
    
    classDef inbound fill:#4A90E2,stroke:#2E5C8A,color:#fff
    classDef application fill:#F5A623,stroke:#C17E1B,color:#fff
    classDef domain fill:#7ED321,stroke:#5FA119,color:#000
    classDef outbound fill:#BD10E0,stroke:#8B0AA8,color:#fff
    classDef external fill:#D0021B,stroke:#8B0000,color:#fff
    
    class A inbound
    class B application
    class C domain
    class D,E outbound
    class F,G external
```

---

## Key Principles

| Aspect | Description |
|--------|-------------|
| **Domain Purity** | Domain layer has ZERO external dependencies. No Spring, JPA, Lombok annotations. |
| **Port Abstraction** | Application defines ports (interfaces) for both inbound and outbound. |
| **Adapter Implementation** | External frameworks are kept in adapters (REST, JPA, Kafka). |
| **Dependency Inversion** | Business logic depends on abstractions (ports), not concrete implementations. |
| **Testability** | Domain & application logic tested with mocks; adapters tested separately. |
| **Framework Agnostic** | Domain logic could be reused in different frameworks without changes. |

---

## Example Flow: Create Transaction Request

```mermaid
sequenceDiagram
    participant Client as 🌐 Client
    participant Controller as REST Controller<br/>Adapter (Inbound)
    participant Mapper as DTOs & Mapper<br/>Adapter (Inbound)
    participant UseCase as CreateTransactionUseCase<br/>Port (Application)
    participant Service as CreateTransactionService<br/>Service (Application)
    participant Domain as Domain Model<br/>Pure Layer
    participant PersistPort as TransactionPersistencePort<br/>Port (Outbound)
    participant JpaAdapter as TransactionJpaAdapter<br/>Adapter (Outbound)
    participant DB as PostgreSQL<br/>Infrastructure

    Client->>Controller: POST /transactions<br/>{terminalId, amount, type}
    Controller->>Mapper: Map DTO to Domain Request
    Mapper->>UseCase: Call createTransaction()
    UseCase->>Service: (Implements Port)
    Service->>Domain: Create & Validate
    Domain-->>Service: Domain Object
    Service->>PersistPort: save(domainObject)
    PersistPort->>JpaAdapter: (Implemented by Adapter)
    JpaAdapter->>DB: Persist Entity
    DB-->>JpaAdapter: Success
    JpaAdapter-->>PersistPort: Return
    PersistPort-->>Service: Success
    Service-->>UseCase: Transaction Created
    UseCase-->>Mapper: Domain to Response DTO
    Mapper-->>Controller: Response Record
    Controller-->>Client: 201 Created<br/>{id, status, ...}
```

---

## Testing Strategy

| Layer | Test Type | Tools |
|-------|-----------|-------|
| **Domain** | Unit Tests | JUnit 5 (no mocks needed - pure logic) |
| **Application Services** | Unit Tests | JUnit 5 + Mockito (mock ports) |
| **Inbound Adapters** | Integration Tests | @SpringBootTest + MockMvc |
| **Outbound Adapters** | Integration Tests | Testcontainers (PostgreSQL, Kafka) |
| **End-to-End** | Integration Tests | @SpringBootTest + Testcontainers |

