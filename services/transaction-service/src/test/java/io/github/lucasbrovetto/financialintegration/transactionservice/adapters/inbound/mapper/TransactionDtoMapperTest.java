package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.mapper;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionDtoMapperTest {

    private final TransactionDtoMapper mapper = Mappers.getMapper(TransactionDtoMapper.class);

    @Test
    @DisplayName("Create transaction request maps to a create transaction command")
    void toCommand_mapsRequestFields() {
        var request = new CreateTransactionRequest(
                "POS-UR-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );

        var command = mapper.toCommand(request);

        assertEquals(request.terminalId(), command.terminalId());
        assertEquals(request.amount(), command.amount());
        assertEquals(request.type(), command.type());
    }

    @Test
    @DisplayName("Transaction maps to the transaction response DTO")
    void toResponse_mapsTransactionFields() {
        var transaction = Transaction.restore(
                UUID.randomUUID(),
                "POS-UR-001",
                new BigDecimal("150.50"),
                TransactionType.SALE,
                TransactionStatus.PENDING,
                LocalDateTime.of(2026, 7, 15, 10, 0),
                LocalDateTime.of(2026, 7, 15, 10, 1),
                null
        );

        var response = mapper.toResponse(transaction);

        assertEquals(transaction.getId(), response.id());
        assertEquals(transaction.getTerminalId(), response.terminalId());
        assertEquals(transaction.getAmount(), response.amount());
        assertEquals(transaction.getType(), response.type());
        assertEquals(transaction.getStatus(), response.status());
        assertEquals(transaction.getCreatedAt(), response.createdAt());
        assertEquals(transaction.getUpdatedAt(), response.updatedAt());
        assertEquals(transaction.getFailureReason(), response.failureReason());
    }
}
