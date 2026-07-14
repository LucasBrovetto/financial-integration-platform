package io.github.lucasbrovetto.financialintegration.transactionservice.domain.model;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    @DisplayName("Create transaction - success creates a valid transaction")
    void createTransaction_success() {
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
    @DisplayName("Create transaction - blank terminal id throws exception")
    void createTransaction_blankTerminalId()  {
        var amount = new BigDecimal("100.00");

        var exception = assertThrows(InvalidTransactionException.class,
                () -> Transaction.create(
                        "",
                        amount,
                        TransactionType.SALE
                ));

        assertEquals("Terminal ID cannot be null or empty", exception.getMessage());

    }

    @Test
    @DisplayName("Create transaction - negative amount throws exception")
    void createTransaction_negativeAmount() {
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
    @DisplayName("Create transaction - null transaction type throws exception")
    void createTransaction_nullTransactionType() {
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
    @DisplayName("Approve transaction - success changes status to APPROVED")
    void approveTransaction_success() {
        var terminalId = "TERM-001";
        var amount = new BigDecimal("150.50");
        var type = TransactionType.SALE;

        var transaction = Transaction.create(terminalId, amount, type);

        transaction.approve();

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
    }

    @Test
    @DisplayName("Approve transaction - non pending transaction throws exception")
    void approveTransaction_nonPending() {
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
    @DisplayName("Decline transaction - success changes status to DECLINED")
    void declineTransaction_success() {
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
    @DisplayName("Decline transaction - non pending transaction throws exception")
    void declineTransaction_nonPending() {
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
