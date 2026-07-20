package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction JPA Entity
 * Outbound Adapter: Maps domain Transaction to database table
 * This is SEPARATE from the domain model
 * No domain logic here - only persistence mapping
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String terminalId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private String failureReason;
}

