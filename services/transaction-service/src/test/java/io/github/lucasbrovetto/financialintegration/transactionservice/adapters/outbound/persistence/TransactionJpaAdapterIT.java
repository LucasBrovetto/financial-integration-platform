package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence;

import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
class TransactionJpaAdapterIT {


    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17"))
                    .withDatabaseName("financial_platform")
                    .withUsername("postgres")
                    .withPassword("12345");

    @Autowired
    private TransactionPersistencePort transactionPersistencePort;

    @Test
    @DisplayName("Save transaction - success persists transaction")
    void saveTransaction_success() {
        var transaction = createTransaction();

        var savedTransaction = transactionPersistencePort.save(transaction);

        assertNotNull(savedTransaction);
        assertNotNull(savedTransaction.getId());
        assertEquals("TERM-001", savedTransaction.getTerminalId());
        assertEquals(new BigDecimal("150.50"), savedTransaction.getAmount());
        assertEquals(TransactionType.SALE, savedTransaction.getType());
        assertEquals(TransactionStatus.PENDING, savedTransaction.getStatus());
    }

    @Test
    @DisplayName("Find transaction by id - success returns transaction")
    void findTransactionById_success() {
        var transaction = createTransaction();

        var savedTransaction = transactionPersistencePort.save(transaction);

        var foundTransaction = transactionPersistencePort.findById(savedTransaction.getId())
                .orElseThrow();

        assertEquals(savedTransaction.getId(), foundTransaction.getId());
        assertEquals("TERM-001", foundTransaction.getTerminalId());
        assertEquals(new BigDecimal("150.50"), foundTransaction.getAmount());
        assertEquals(TransactionType.SALE, foundTransaction.getType());
        assertEquals(TransactionStatus.PENDING, foundTransaction.getStatus());
    }

    @Test
    @DisplayName("Find transaction by id - not found returns empty")
    void findTransactionById_notFound() {
        var transactionId = UUID.randomUUID();
        var result = transactionPersistencePort.findById(transactionId);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Exists transaction by id - success returns true")
    void existsTransactionById_success() {
        var transaction = createTransaction();
        var savedTransaction = transactionPersistencePort.save(transaction);
        var exists = transactionPersistencePort.existsById(savedTransaction.getId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists transaction by id - not found returns false")
    void existsTransactionById_notFound() {
        var exists = transactionPersistencePort.existsById(UUID.randomUUID());
        assertFalse(exists);
    }

    private Transaction createTransaction() {
        return Transaction.create(
                "TERM-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );
    }
}
