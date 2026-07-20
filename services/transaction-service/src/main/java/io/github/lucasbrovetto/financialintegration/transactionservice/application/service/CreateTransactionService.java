package io.github.lucasbrovetto.financialintegration.transactionservice.application.service;

import io.github.lucasbrovetto.financialintegration.transactionservice.application.exception.TransactionPersistenceException;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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

        // Step 1: Create domain transaction (domain rules applied in factory method)
        Transaction transaction = Transaction.create(
                command.terminalId(),
                command.amount(),
                command.type()
        );

        // Step 2: Persist transaction
        try {
            Transaction savedTransaction = transactionPersistencePort.save(transaction);
            log.info("Transaction created successfully: {}", savedTransaction.getId());

            return savedTransaction;

        } catch (DataAccessException e) {
            log.error("Error persisting transaction {}", transaction.getId(), e);
            throw new TransactionPersistenceException("Failed to persist transaction", e);
        }

    }
}

