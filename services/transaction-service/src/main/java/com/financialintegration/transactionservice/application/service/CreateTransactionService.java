package com.financialintegration.transactionservice.application.service;

import com.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import com.financialintegration.transactionservice.application.port.in.CreateTransactionUseCase;
import com.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import com.financialintegration.transactionservice.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application Service: Create Transaction Use Case Implementation
 * Orchestrates domain logic and coordinates with persistence port.
 * This service is framework-agnostic; it depends on abstractions (ports).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTransactionService implements CreateTransactionUseCase {

    private final TransactionPersistencePort transactionPersistencePort;

    /**
     * Execute the create transaction use case
     * Steps:
     * 1. Create domain Transaction from command
     * 2. Validate business rules (delegated to domain)
     * 3. Persist transaction
     * 4. Return created transaction
     */
    @Override
    public Transaction execute(CreateTransactionCommand command) {
        log.info("Creating transaction for terminal: {}, amount: {}, type: {}",
                command.terminalId(), command.amount(), command.type());

        try {
            // Step 1: Create domain transaction (domain rules applied in factory method)
            Transaction transaction = Transaction.create(
                    command.terminalId(),
                    command.amount(),
                    command.type()
            );

            // Step 2: Persist transaction
            Transaction savedTransaction = transactionPersistencePort.save(transaction);
            log.info("Transaction created successfully: {}", savedTransaction.getId());

            return savedTransaction;

        } catch (IllegalArgumentException e) {
            log.error("Invalid transaction request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating transaction", e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }
}

