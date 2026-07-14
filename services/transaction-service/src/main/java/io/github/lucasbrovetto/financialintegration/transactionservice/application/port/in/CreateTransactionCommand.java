package io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionCommand(
        String terminalId,
        BigDecimal amount,
        TransactionType type) {
}


