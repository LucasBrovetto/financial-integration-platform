package com.financialintegration.transactionservice.application.port.in;

import com.financialintegration.transactionservice.domain.model.Transaction;

/**
 * Input Port (Use Case): Create Transaction
 * Defines the contract for creating a new transaction.
 * This is independent of HTTP, Spring, or any framework.
 */
public interface CreateTransactionUseCase {

    /**
     * Execute the create transaction use case
     *
     * @param command The create transaction command with required data
     * @return The created transaction
     */
    Transaction execute(CreateTransactionCommand command);


}

