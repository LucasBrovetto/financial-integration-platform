# Transaction Processing Flows

## 1️⃣ Happy Path: Successful Transaction (APPROVED)

```mermaid
sequenceDiagram
    participant POS as 🖥️ POS Terminal
    participant Gateway as 🚪 API Gateway
    participant TS as 📊 Transaction Service
    participant Auth as 🔐 Auth Service
    participant Config as 🖥️ Config Service
    participant Acquirer as 🏦 Acquirer Simulator
    participant DB as 🗄️ PostgreSQL
    participant Kafka as 📨 Kafka

    autonumber

    POS->>Gateway: POST /transactions<br/>{terminalId, amount, type}
    
    Gateway->>TS: Route to Transaction Service
    
    TS->>TS: Validate Request<br/>(Domain Layer)
    
    TS->>Config: GET /terminal/{terminalId}<br/>config check
    
    Config->>Config: Check Redis Cache
    
    Config-->>TS: Terminal Config {limits, enabled}
    
    TS->>TS: Validate Against Limits<br/>(Domain Logic)
    
    TS->>Auth: POST /authorize<br/>{terminalId, amount}
    
    Auth->>Acquirer: POST /authorize<br/>(retry: 3 attempts)
    
    Acquirer-->>Auth: 200 APPROVED
    
    Auth-->>TS: APPROVED
    
    TS->>DB: INSERT Transaction<br/>status=APPROVED<br/>timestamp=now()
    
    DB-->>TS: Transaction ID + Metadata
    
    TS->>Kafka: PUBLISH event<br/>transaction.approved<br/>{id, terminalId, amount, timestamp}
    
    Kafka-->>TS: Event Persisted
    
    TS-->>Gateway: 201 Created<br/>{id, status, terminalId, amount}
    
    Gateway-->>POS: 201 APPROVED<br/>{transactionId}
    
    Note over Kafka: Auth Service subscribes<br/>to update terminal limits
```

---

## 2️⃣ Sad Path: Transaction Declined (DECLINED)

```mermaid
sequenceDiagram
    participant POS as 🖥️ POS Terminal
    participant Gateway as 🚪 API Gateway
    participant TS as 📊 Transaction Service
    participant Auth as 🔐 Auth Service
    participant Acquirer as 🏦 Acquirer Simulator
    participant DB as 🗄️ PostgreSQL
    participant Kafka as 📨 Kafka

    autonumber

    POS->>Gateway: POST /transactions<br/>{terminalId, amount=5000}
    
    Gateway->>TS: Route to Transaction Service
    
    TS->>TS: Validate Request
    
    TS->>TS: Check Terminal Limits<br/>Daily Limit: $1000<br/>⚠️ EXCEEDS!
    
    TS->>DB: INSERT Transaction<br/>status=DECLINED<br/>reason=LIMIT_EXCEEDED<br/>timestamp=now()
    
    TS->>Kafka: PUBLISH event<br/>transaction.declined<br/>{id, reason, terminalId}
    
    TS-->>Gateway: 422 Unprocessable Entity<br/>{code: LIMIT_EXCEEDED,<br/> message: Daily limit exceeded}
    
    Gateway-->>POS: 422 DECLINED<br/>Reason: Limit Exceeded
    
    Note over Kafka: Config Service may adjust<br/>terminal limits based on events
```

**Alternative Decline Path: Acquirer Rejects**

```mermaid
sequenceDiagram
    participant TS as 📊 Transaction Service
    participant Auth as 🔐 Auth Service
    participant Acquirer as 🏦 Acquirer Simulator
    participant DB as 🗄️ PostgreSQL
    participant Kafka as 📨 Kafka

    autonumber

    TS->>Auth: POST /authorize
    
    Auth->>Acquirer: POST /authorize<br/>(retry: 3 attempts)
    
    Acquirer-->>Auth: 403 DECLINED<br/>{code: INSUFFICIENT_FUNDS}
    
    Auth-->>TS: DECLINED
    
    TS->>DB: INSERT Transaction<br/>status=DECLINED<br/>reason=INSUFFICIENT_FUNDS
    
    TS->>Kafka: PUBLISH event<br/>transaction.declined
    
    TS-->>Gateway: 200 OK<br/>{id, status=DECLINED, reason}
```

---

## 3️⃣ Error Path: Retry Logic with Circuit Breaker

```mermaid
sequenceDiagram
    participant TS as 📊 Transaction Service
    participant Auth as 🔐 Auth Service
    participant Acquirer as 🏦 Acquirer Simulator
    participant CB as ⚡ Circuit Breaker<br/>Resilience4j
    participant DB as 🗄️ PostgreSQL
    participant Kafka as 📨 Kafka

    autonumber

    TS->>CB: Request Authorization<br/>(Acquirer Status: CLOSED)
    
    CB->>Auth: POST /authorize<br/>Attempt 1
    
    Auth->>Acquirer: POST /authorize
    
    Note over Acquirer: ❌ Timeout (5s)
    
    Acquirer-->>Auth: Connection Timeout
    
    Auth-->>CB: Retry Exception
    
    CB->>Auth: POST /authorize<br/>Attempt 2
    
    Auth->>Acquirer: POST /authorize
    
    Note over Acquirer: ❌ 500 Internal Error
    
    Acquirer-->>Auth: 500 Error
    
    Auth-->>CB: Retry Exception
    
    CB->>Auth: POST /authorize<br/>Attempt 3 (Last)
    
    Auth->>Acquirer: POST /authorize
    
    Note over Acquirer: ❌ Service Unavailable
    
    Acquirer-->>Auth: 503 Unavailable
    
    Auth-->>CB: Max Retries Exceeded
    
    CB->>CB: OPEN Circuit Breaker<br/>Fail Fast Mode
    
    CB-->>TS: CallNotPermittedException
    
    TS->>DB: INSERT Transaction<br/>status=PENDING<br/>reason=SERVICE_UNAVAILABLE
    
    TS->>Kafka: PUBLISH event<br/>transaction.pending_retry<br/>{id, reason: SERVICE_UNAVAILABLE,<br/> retryAfter: 30s}
    
    TS-->>Gateway: 503 Service Unavailable<br/>{message: Processing delayed,<br/> transactionId: for tracking}
    
    Note over TS: Async Job: Retry after 30s
```

---

## 4️⃣ Event-Driven Processing (Kafka Topics)

```mermaid
graph TB
    subgraph "Transaction Service"
        TS["📊 Transaction Service<br/>Event Publisher"]
    end

    subgraph "Kafka Topics"
        CREATED["📨 transaction.created<br/>Schema: {id, terminalId, amount, type, timestamp}"]
        APPROVED["📨 transaction.approved<br/>Schema: {id, terminalId, status, timestamp}"]
        DECLINED["📨 transaction.declined<br/>Schema: {id, terminalId, reason, timestamp}"]
        PENDING["📨 transaction.pending_retry<br/>Schema: {id, reason, retryAfter}"]
    end

    subgraph "Subscribers"
        AUTH_SUB["🔐 Auth Service<br/>→ Update terminal limits"]
        ANALYTICS["📊 Analytics Service<br/>→ Track metrics"]
        NOTIFICATION["🔔 Notification Service<br/>→ Send alerts"]
        AUDIT["📝 Audit Log Service<br/>→ Compliance"]
    end

    TS -->|Publish| CREATED
    TS -->|Publish| APPROVED
    TS -->|Publish| DECLINED
    TS -->|Publish| PENDING

    CREATED -->|Subscribe| AUTH_SUB
    CREATED -->|Subscribe| ANALYTICS
    CREATED -->|Subscribe| AUDIT

    APPROVED -->|Subscribe| ANALYTICS
    APPROVED -->|Subscribe| NOTIFICATION
    APPROVED -->|Subscribe| AUDIT

    DECLINED -->|Subscribe| ANALYTICS
    DECLINED -->|Subscribe| NOTIFICATION
    DECLINED -->|Subscribe| AUDIT

    PENDING -->|Subscribe| ANALYTICS
    PENDING -->|Subscribe| AUTH_SUB

    style TS fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style CREATED fill:#50E3C2,stroke:#2BA39A,color:#000
    style APPROVED fill:#50E3C2,stroke:#2BA39A,color:#000
    style DECLINED fill:#50E3C2,stroke:#2BA39A,color:#000
    style PENDING fill:#50E3C2,stroke:#2BA39A,color:#000
```

---

## 5️⃣ Compensating Transaction (Refund/Reversal)

```mermaid
sequenceDiagram
    participant POS as 🖥️ POS Terminal
    participant Gateway as 🚪 API Gateway
    participant TS as 📊 Transaction Service
    participant Auth as 🔐 Auth Service
    participant Acquirer as 🏦 Acquirer Simulator
    participant DB as 🗄️ PostgreSQL
    participant Kafka as 📨 Kafka

    autonumber

    Note over POS,Kafka: User initiates REFUND for<br/>previously approved transaction

    POS->>Gateway: POST /transactions/refund<br/>{originalTransactionId}
    
    Gateway->>TS: Route to Transaction Service
    
    TS->>DB: SELECT Transaction<br/>WHERE id = originalTransactionId<br/>AND status = APPROVED
    
    DB-->>TS: Original Transaction Data
    
    TS->>TS: Create Compensating Transaction<br/>type=REFUND<br/>amount=-original_amount<br/>linkedTransactionId=original_id
    
    TS->>Auth: POST /refund<br/>{originalAuthCode, amount}
    
    Auth->>Acquirer: POST /refund<br/>(retry: 3 attempts)
    
    Acquirer-->>Auth: 200 APPROVED
    
    Auth-->>TS: REFUND_APPROVED
    
    TS->>DB: INSERT Refund Transaction<br/>status=APPROVED<br/>linkedTransactionId=original_id
    
    TS->>Kafka: PUBLISH event<br/>transaction.refunded<br/>{originalId, refundId, amount}
    
    TS-->>Gateway: 201 Created<br/>{refundId, status=APPROVED}
    
    Gateway-->>POS: 201 REFUND_APPROVED
    
    Note over Kafka: Auth Service updates limits<br/>Analytics tracks refund metrics
```

---

## 6️⃣ Transaction Status Timeline

```mermaid
stateDiagram-v2
    [*] --> PENDING
    
    PENDING --> APPROVED: Authorize Success
    PENDING --> DECLINED: Validation Failed OR<br/>Acquirer Declined
    PENDING --> PENDING_RETRY: Service Error (3 retries)
    
    APPROVED --> REFUNDED: Refund Request
    APPROVED --> REVERSED: Reversal Request
    
    DECLINED --> [*]
    REFUNDED --> [*]
    REVERSED --> [*]
    
    PENDING_RETRY --> APPROVED: Async Retry Success
    PENDING_RETRY --> DECLINED: Async Retry Declined
    PENDING_RETRY --> EXPIRED: Retry Timeout (24h)
    
    EXPIRED --> [*]
    
    note right of PENDING
        Initial state
        Waiting for authorization
    end note
    
    note right of APPROVED
        Terminal state
        Successfully authorized
    end note
    
    note right of DECLINED
        Terminal state
        Failed authorization
    end note
    
    note right of PENDING_RETRY
        Service error occurred
        Scheduled for retry (30s-24h)
    end note
```

---

## Key Patterns & Resilience Features

| Pattern | Implementation | Benefit |
|---------|----------------|---------|
| **Retry Logic** | Exponential backoff + 3 retries | Handles transient failures |
| **Circuit Breaker** | Resilience4j configuration | Prevents cascading failures |
| **Fallback** | Return PENDING status + async retry | Graceful degradation |
| **Event Sourcing** | Kafka topics persist all events | Audit trail + async processing |
| **Compensation** | Refund/Reversal transactions | Handles business reversals |
| **Idempotency** | Transaction ID as unique key | Safe retries without duplication |
| **Timeout Protection** | Request timeouts (5s) | Prevents hanging requests |

---

## Error Codes & Responses

| Status | Code | Meaning | Retry? |
|--------|------|---------|--------|
| 201 | OK | Transaction approved | ❌ Terminal |
| 422 | LIMIT_EXCEEDED | Daily/terminal limit exceeded | ❌ Terminal |
| 422 | INSUFFICIENT_FUNDS | Acquirer: insufficient funds | ❌ Terminal |
| 422 | INVALID_TERMINAL | Terminal not found/disabled | ❌ Terminal |
| 503 | SERVICE_UNAVAILABLE | Auth/Acquirer unreachable | ✅ Async Retry |
| 500 | PROCESSING_ERROR | Internal server error | ✅ Async Retry |

