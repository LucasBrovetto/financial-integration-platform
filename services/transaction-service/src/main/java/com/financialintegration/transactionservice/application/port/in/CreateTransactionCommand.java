package com.financialintegration.transactionservice.application.port.in;

import com.financialintegration.transactionservice.domain.model.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionCommand(
        String terminalId,
        BigDecimal amount,
        TransactionType type) {
}


