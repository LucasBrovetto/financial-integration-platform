package io.github.lucasbrovetto.financialintegration.transactionservice.application.port.out;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;

import java.util.Optional;
import java.util.UUID;

/**
 * Output Port: Transaction Persistence
 * Defines the contract for persisting and retrieving transactions.
 * Implementation can be JPA, MongoDB, or any persistence technology.
 * The domain layer depends on this abstraction, not on concrete persistence.
 */
public interface TransactionPersistencePort {

    /**
     * Save a transaction to persistence
     *
     * @param transaction The transaction to save
     * @return The saved transaction
     */
    Transaction save(Transaction transaction);

    /**
     * Find a transaction by its ID
     *
     * @param id The transaction identifier
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Check if a transaction exists by ID
     *
     * @param id The transaction identifier
     * @return true if transaction exists, false otherwise
     */
    boolean existsById(UUID id);

    /**
     * Update an existing transaction
     *
     * @param transaction The transaction to update
     * @return The updated transaction
     */
    Transaction update(Transaction transaction);

    /**
     * Delete a transaction by ID
     *
     * @param id The transaction identifier
     */
    void deleteById(UUID id);
}

