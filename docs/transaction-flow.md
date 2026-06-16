# Sale Transaction

```mermaid
sequenceDiagram

participant POS

participant Gateway

participant Auth

participant Config

participant Acquirer

participant DB

POS->>Gateway: SALE 100

Gateway->>Auth: Process Transaction

Auth->>Config: Load Terminal

Config-->>Auth: Terminal Config

Auth->>Acquirer: Authorize

Acquirer-->>Auth: APPROVED

Auth->>DB: Save Transaction

Auth-->>Gateway: APPROVED

Gateway-->>POS: APPROVED
```
