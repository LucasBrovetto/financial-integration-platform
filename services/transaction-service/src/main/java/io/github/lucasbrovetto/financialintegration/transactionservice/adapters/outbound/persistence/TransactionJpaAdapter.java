package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence.mapper.TransactionPersistenceMapper;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Persistence Adapter
 * Outbound Adapter: Implements TransactionPersistencePort using JPA/Hibernate
 * Responsible for:
 * 1. Converting domain Transaction models to/from JPA entities
 * 2. Delegating database operations to Spring Data JPA
 * 3. Keeping domain logic isolated from persistence details
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionJpaAdapter implements TransactionPersistencePort {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionPersistenceMapper transactionPersistenceMapper;

    /**
     * Save a transaction to the database
     * Converts domain Transaction → JPA Entity → persist → convert back
     */
    @Override
    public Transaction save(Transaction transaction) {
        log.debug("Persisting transaction: {}", transaction.getId());

        TransactionEntity entity = transactionPersistenceMapper.toEntity(transaction);
        TransactionEntity savedEntity = jpaRepository.save(entity);

        log.debug("Transaction persisted successfully: {}", savedEntity.getId());
        return transactionPersistenceMapper.toDomain(savedEntity);
    }

    /**
     * Find transaction by UUID
     */
    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpaRepository.findById(id).map(transactionPersistenceMapper::toDomain);
    }

    /**
     * Check if transaction exists
     */
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    /**
     * Update an existing transaction
     */
    @Override
    public Transaction update(Transaction transaction) {
        log.debug("Updating transaction: {}", transaction.getId());

        if (!existsById(transaction.getId())) {
            throw new TransactionNotFoundException(transaction.getId());
        }

        TransactionEntity entity = transactionPersistenceMapper.toEntity(transaction);
        TransactionEntity updatedEntity = jpaRepository.save(entity);

        log.debug("Transaction updated successfully: {}", updatedEntity.getId());
        return transactionPersistenceMapper.toDomain(updatedEntity);
    }

    /**
     * Delete transaction
     */
    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting transaction: {}", id);
        jpaRepository.deleteById(id);
        log.debug("Transaction deleted: {}", id);
    }

}
