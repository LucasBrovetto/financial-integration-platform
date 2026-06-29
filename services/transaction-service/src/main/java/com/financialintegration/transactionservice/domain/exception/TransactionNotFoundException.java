package com.financialintegration.transactionservice.domain.exception;

import lombok.Getter;

/**
 * Exception thrown when a transaction is not found
 */
@Getter
public class TransactionNotFoundException extends RuntimeException {

    private final String transactionId;

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found: " + transactionId);
        this.transactionId = transactionId;
    }

}

