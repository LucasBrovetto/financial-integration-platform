package com.financialintegration.transactionservice.application.exception;

public class TransactionPersistenceException extends RuntimeException {

    public TransactionPersistenceException(String message) {
        super(message);
    }

    public TransactionPersistenceException(String message, Throwable cause) {

        super(message, cause);

    }
}
