# Deployment

```mermaid
flowchart TB

Docker[Docker Network]

TS[Transaction Service]

ACQ[Acquirer Simulator]

DB[(PostgreSQL)]

REDIS[(Redis)]

Docker --> TS

Docker --> ACQ

Docker --> DB

Docker --> REDIS
```
