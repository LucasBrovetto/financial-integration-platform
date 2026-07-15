package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence.mapper;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence.TransactionEntity;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionPersistenceMapperTest {

    private final TransactionPersistenceMapper mapper = Mappers.getMapper(TransactionPersistenceMapper.class);

    @Test
    @DisplayName("Transaction to entity maps every persistence field")
    void toEntity_mapsAllTransactionFields() {
        var transaction = Transaction.restore(
                UUID.randomUUID(),
                "POS-UR-001",
                new BigDecimal("150.50"),
                TransactionType.SALE,
                TransactionStatus.DECLINED,
                LocalDateTime.of(2026, 7, 15, 10, 0),
                LocalDateTime.of(2026, 7, 15, 10, 1),
                "Insufficient funds"
        );

        var entity = mapper.toEntity(transaction);

        assertEquals(transaction.getId(), entity.getId());
        assertEquals(transaction.getTerminalId(), entity.getTerminalId());
        assertEquals(transaction.getAmount(), entity.getAmount());
        assertEquals(transaction.getType(), entity.getType());
        assertEquals(transaction.getStatus(), entity.getStatus());
        assertEquals(transaction.getCreatedAt(), entity.getCreatedAt());
        assertEquals(transaction.getUpdatedAt(), entity.getUpdatedAt());
        assertEquals(transaction.getFailureReason(), entity.getFailureReason());
    }

    @Test
    @DisplayName("Entity to transaction restores every domain field")
    void toDomain_restoresAllTransactionFields() {
        var entity = TransactionEntity.builder()
                .id(UUID.randomUUID())
                .terminalId("POS-UR-001")
                .amount(new BigDecimal("150.50"))
                .type(TransactionType.SALE)
                .status(TransactionStatus.DECLINED)
                .createdAt(LocalDateTime.of(2026, 7, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 7, 15, 10, 1))
                .failureReason("Insufficient funds")
                .build();

        var transaction = mapper.toDomain(entity);

        assertEquals(entity.getId(), transaction.getId());
        assertEquals(entity.getTerminalId(), transaction.getTerminalId());
        assertEquals(entity.getAmount(), transaction.getAmount());
        assertEquals(entity.getType(), transaction.getType());
        assertEquals(entity.getStatus(), transaction.getStatus());
        assertEquals(entity.getCreatedAt(), transaction.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), transaction.getUpdatedAt());
        assertEquals(entity.getFailureReason(), transaction.getFailureReason());
    }
}
