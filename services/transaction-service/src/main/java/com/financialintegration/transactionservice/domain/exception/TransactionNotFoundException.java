package com.financialintegration.transactionservice.domain.exception;

import lombok.Getter;

import java.util.UUID;

/**
 * Exception thrown when a transaction is not found
 */
@Getter
public class TransactionNotFoundException extends RuntimeException {

    private final UUID transactionId;

    public TransactionNotFoundException(UUID transactionId) {
        super("Transaction not found: " + transactionId);
        this.transactionId = transactionId;
    }

}

