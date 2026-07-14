package com.financialintegration.transactionservice.application;

import com.financialintegration.transactionservice.application.exception.TransactionPersistenceException;
import com.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import com.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import com.financialintegration.transactionservice.application.service.CreateTransactionService;
import com.financialintegration.transactionservice.domain.model.Transaction;
import com.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionServiceTest {

    @Mock
    private TransactionPersistencePort transactionPersistencePort;

    @InjectMocks
    private CreateTransactionService createTransactionService;

    @Test
    @DisplayName("Create transaction - success persists transaction")
    void createTransaction_success() {
        var command = new CreateTransactionCommand(
                "TERM-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );

        when(transactionPersistencePort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = createTransactionService.execute(command);

        assertNotNull(result);
        assertEquals("TERM-001", result.getTerminalId());
        assertEquals(new BigDecimal("150.50"), result.getAmount());
        assertEquals(TransactionType.SALE, result.getType());
        verify(transactionPersistencePort).save(any(Transaction.class));

    }

    @Test
    @DisplayName("Create transaction - persistence failure throws exception")
    void createTransaction_persistenceFailure() {
        var command = new CreateTransactionCommand(
                "TERM-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );

        when(transactionPersistencePort.save(any(Transaction.class)))
                .thenThrow(new DataAccessResourceFailureException("Database error"));
        var exception = assertThrows(
                TransactionPersistenceException.class,
                () -> createTransactionService.execute(command)
        );

        assertEquals("Failed to persist transaction", exception.getMessage());
        assertTrue(exception.getCause() instanceof DataAccessResourceFailureException);
        verify(transactionPersistencePort).save(any(Transaction.class));
    }
}
