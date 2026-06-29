package com.financialintegration.transactionservice.application.port.in;

import com.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import com.financialintegration.transactionservice.domain.model.Transaction;

/**
 * Input Port (Use Case): Get Transaction by ID
 * Defines the contract for retrieving a transaction by its identifier.
 */
public interface GetTransactionUseCase {

    /**
     * Execute the get transaction use case
     *
     * @param transactionId The transaction identifier
     * @return The transaction if found
     * @throws TransactionNotFoundException
     *         if the transaction is not found
     */
    Transaction execute(String transactionId);
}

