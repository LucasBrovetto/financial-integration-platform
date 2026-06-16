# Transaction Flow

```mermaid
sequenceDiagram

POS->>TransactionService: SALE 100

TransactionService->>Redis: Load Config

Redis-->>TransactionService: Config

TransactionService->>Acquirer: Authorize

Acquirer-->>TransactionService: APPROVED

TransactionService->>PostgreSQL: Save Transaction

TransactionService-->>POS: APPROVED
```
