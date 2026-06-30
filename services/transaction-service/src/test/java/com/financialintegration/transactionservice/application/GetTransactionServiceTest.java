package com.financialintegration.transactionservice.application;

import com.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import com.financialintegration.transactionservice.application.service.GetTransactionService;
import com.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import com.financialintegration.transactionservice.domain.model.Transaction;
import com.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetTransactionServiceTest {

    @Mock
    private TransactionPersistencePort transactionPersistencePort;

    @InjectMocks
    private GetTransactionService getTransactionService;

    @Test
    void shouldReturnTransactionWhenTransactionExists() {
        var transactionId = UUID.randomUUID().toString();
        var transaction = Transaction.create(
                "TERM-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );

        when(transactionPersistencePort.findByIdString(transactionId))
                .thenReturn(Optional.of(transaction));

        var result = getTransactionService.execute(transactionId);

        assertNotNull(result);
        assertSame(transaction, result);
        verify(transactionPersistencePort).findByIdString(transactionId);

    }

    @Test
    void shouldThrowTransactionNotFoundExceptionWhenTransactionDoesNotExist() {
        var transactionId = UUID.randomUUID().toString();

        when(transactionPersistencePort.findByIdString(transactionId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(TransactionNotFoundException.class,
                () -> getTransactionService.execute(transactionId)
        );

        assertEquals("Transaction not found: " + transactionId, exception.getMessage());
        verify(transactionPersistencePort).findByIdString(transactionId);

    }
}
