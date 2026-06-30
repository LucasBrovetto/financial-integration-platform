package com.financialintegration.transactionservice.domain.model;

import com.financialintegration.transactionservice.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void shouldCreateTransactionSuccessfully() {

        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals(terminalId, transaction.getTerminalId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(type, transaction.getType());
        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertNotNull(transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());
        assertNull(transaction.getFailureReason());

    }

    @Test
    void shouldThrowExceptionWhenTerminalIdIsBlank() {
        var amount = new BigDecimal("100.00");

        var exception = assertThrows(InvalidTransactionException.class,
                () -> Transaction.create(
                        EMPTY,
                        amount,
                        TransactionType.SALE
                ));

        assertEquals("Terminal ID cannot be null or empty", exception.getMessage());

    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("-100.00");

        var exception = assertThrows(InvalidTransactionException.class,
                () -> Transaction.create(
                        terminalId,
                        amount,
                        TransactionType.SALE
                ));

        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransactionTypeIsNull() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("100.00");

        var exception = assertThrows(InvalidTransactionException.class,
                () -> Transaction.create(
                        terminalId,
                        amount,
                        null
                ));

        assertEquals("Transaction type cannot be null", exception.getMessage());
    }

    @Test
    void shouldApprovePendingTransaction() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        transaction.approve();

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenApprovingNonPendingTransaction() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        //Approve transaction first
        transaction.approve();

        var exception = assertThrows(InvalidTransactionException.class, transaction::approve);

        assertEquals("Only PENDING transactions can be approved", exception.getMessage());
    }

    @Test
    void shouldDeclinePendingTransaction() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        var reason = "Insufficient funds";
        transaction.decline(reason);

        assertEquals(TransactionStatus.DECLINED, transaction.getStatus());
        assertEquals(reason, transaction.getFailureReason());

    }

    @Test
    void shouldThrowExceptionWhenDecliningNonPendingTransaction() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        transaction.approve();

        var reason = "Insufficient funds";
        var exception = assertThrows(InvalidTransactionException.class, () -> transaction.decline(reason));

        assertEquals("Only PENDING transactions can be declined", exception.getMessage());

    }

}
