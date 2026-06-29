package com.financialintegration.transactionservice.domain.exception;

/**
 * Exception thrown when a transaction violates business rules
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}

