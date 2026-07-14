package com.financialintegration.transactionservice.application.service;

import com.financialintegration.transactionservice.application.port.in.GetTransactionUseCase;
import com.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import com.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import com.financialintegration.transactionservice.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application Service: Get Transaction Use Case Implementation
 * Orchestrates retrieval of a transaction by ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetTransactionService implements GetTransactionUseCase {

    private final TransactionPersistencePort transactionPersistencePort;

    /**
     * Execute the get transaction use case
     * Steps:
     * 1. Query persistence for transaction
     * 2. Return transaction or throw exception if not found
     */
    @Override
    public Transaction execute(UUID transactionId) {
        log.info("Retrieving transaction: {}", transactionId);

        return transactionPersistencePort.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found: {}", transactionId);
                    return new TransactionNotFoundException(transactionId);
                });
    }
}

