package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA Repository for TransactionEntity
 * Internal to the persistence adapter - not exposed outside
 * Used by TransactionJpaAdapter to delegate database operations
 */
@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

}

