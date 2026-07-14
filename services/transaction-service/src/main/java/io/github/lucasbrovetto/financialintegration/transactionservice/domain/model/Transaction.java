package io.github.lucasbrovetto.financialintegration.transactionservice.domain.model;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.exception.InvalidTransactionException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction Domain Entity
 * Represents a financial transaction in the system.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Transaction {

    private final UUID id;
    private final String terminalId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime createdAt;
    private TransactionStatus status;
    private LocalDateTime updatedAt;
    private String failureReason;

    /**
     * Factory method to create a new transaction
     */
    public static Transaction create(String terminalId, BigDecimal amount, TransactionType type) {

        validateCreation(terminalId, amount, type);
        return new Transaction(
                UUID.randomUUID(),
                terminalId,
                amount,
                type,
                LocalDateTime.now(),
                TransactionStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * Factory method to create a new transaction from DB
     */
    public static Transaction restore(
            UUID id,
            String terminalId,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String failureReason) {

        return new Transaction(id, terminalId, amount, type, createdAt, status, updatedAt, failureReason);
    }

    /**
     * Transaction validation
     */
    private static void validateCreation(String terminalId, BigDecimal amount, TransactionType type) {
        if (terminalId == null || terminalId.isBlank()) {
            throw new InvalidTransactionException("Terminal ID cannot be null or empty");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
        if (type == null) {
            throw new InvalidTransactionException("Transaction type cannot be null");
        }
    }

    /**
     * Transaction behavior: Mark transaction as approved
     */
    public void approve() {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Only PENDING transactions can be approved");
        }
        this.status = TransactionStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transaction behavior: Mark transaction as declined with reason
     */
    public void decline(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new InvalidTransactionException("Only PENDING transactions can be declined");
        }
        this.status = TransactionStatus.DECLINED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Transaction behavior: Check if transaction can be refunded
     */
    public boolean canBeRefunded() {
        return this.status == TransactionStatus.APPROVED;
    }

}

