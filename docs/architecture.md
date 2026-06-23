# Microservices Architecture

## Overall System Design

```mermaid
graph TB
    subgraph "External Systems"
        POS["🖥️ POS Simulator"]
        ACQ["🏦 Acquirer Simulator"]
    end

    subgraph "API Gateway & Configuration"
        GW["🚪 Spring Cloud Gateway"]
        CONFIG["⚙️ Config Server<br/>Spring Cloud Config"]
    end

    subgraph "Core Microservices"
        TS["📊 Transaction Service<br/>(Hexagonal Architecture)"]
        AUTH["🔐 Authorization Service"]
        CFG["🖥️ Terminal Configuration Service"]
    end

    subgraph "Data & Messaging Layer"
        DB[("🗄️ PostgreSQL")]
        REDIS[("⚡ Redis Cache")]
        KAFKA["📨 Apache Kafka<br/>Event Streaming"]
    end

    POS -->|HTTP| GW
    GW -->|Routes| TS
    GW -->|Routes| AUTH
    GW -->|Routes| CFG
    
    TS -->|Validate| AUTH
    TS -->|Fetch Config| CFG
    TS -->|Query| REDIS
    TS -->|Publish Events| KAFKA
    TS -->|Persist| DB
    
    AUTH -->|Authorize| ACQ
    AUTH -->|Persist| DB
    
    CFG -->|Cache| REDIS
    CFG -->|Persist| DB
    
    CONFIG -->|Provides Config| TS
    CONFIG -->|Provides Config| AUTH
    CONFIG -->|Provides Config| CFG
    
    KAFKA -->|Subscribe Events| TS
    KAFKA -->|Subscribe Events| AUTH
    
    style TS fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style GW fill:#F5A623,stroke:#C17E1B,color:#fff
    style CONFIG fill:#F5A623,stroke:#C17E1B,color:#fff
    style KAFKA fill:#50E3C2,stroke:#2BA39A,color:#000
```

---

## Design Principles

| Principle | Description |
|-----------|-------------|
| **Hexagonal Architecture** | Each microservice isolates domain logic from external dependencies |
| **Spring Cloud Native** | Leverages Spring Cloud Gateway, Config Server, Circuit Breakers |
| **Event-Driven** | Kafka enables asynchronous, decoupled communication |
| **Database per Service** | Each service owns its PostgreSQL schema (logical isolation) |
| **Caching Strategy** | Redis for terminal configs and frequently accessed data |
| **Resilience** | Retry logic, circuit breakers, fallbacks for external calls |

---

## Technology Stack

- **Runtime**: Java 21 + Spring Boot 3.x
- **API Gateway**: Spring Cloud Gateway
- **Configuration**: Spring Cloud Config
- **Messaging**: Apache Kafka
- **Persistence**: PostgreSQL + Hibernate/JPA
- **Caching**: Redis
- **Build**: Maven 3.9+
- **Container**: Docker + Docker Compose
- **Orchestration**: Kubernetes (future)

