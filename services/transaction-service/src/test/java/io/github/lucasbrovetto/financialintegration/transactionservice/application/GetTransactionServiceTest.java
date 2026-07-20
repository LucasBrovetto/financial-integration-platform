package io.github.lucasbrovetto.financialintegration.transactionservice.application;

import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.out.TransactionPersistencePort;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.service.GetTransactionService;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Get transaction - success returns transaction")
    void getTransaction_success() {
        var transactionId = UUID.randomUUID();
        var transaction = Transaction.create(
                "TERM-001",
                new BigDecimal("150.50"),
                TransactionType.SALE
        );

        when(transactionPersistencePort.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        var result = getTransactionService.execute(transactionId);

        assertNotNull(result);
        assertSame(transaction, result);
        verify(transactionPersistencePort).findById(transactionId);

    }

    @Test
    @DisplayName("Get transaction - transaction not found throws exception")
    void getTransaction_NotFound() {
        var transactionId = UUID.randomUUID();

        when(transactionPersistencePort.findById(transactionId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(TransactionNotFoundException.class,
                () -> getTransactionService.execute(transactionId)
        );

        assertEquals("Transaction not found: " + transactionId, exception.getMessage());
        verify(transactionPersistencePort).findById(transactionId);

    }
}
