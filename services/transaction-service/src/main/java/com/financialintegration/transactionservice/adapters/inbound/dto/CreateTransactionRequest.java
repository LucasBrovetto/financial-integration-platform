package com.financialintegration.transactionservice.adapters.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.financialintegration.transactionservice.domain.model.TransactionType;
import java.math.BigDecimal;

/**
 * Create Transaction Request DTO
 */
public record CreateTransactionRequest(
        @NotBlank(message = "Terminal ID cannot be blank")
        String terminalId,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Transaction type cannot be null")
        TransactionType type
) {
    // Compact constructor with validation
    public CreateTransactionRequest {
        if (terminalId != null) {
            terminalId = terminalId.trim();
        }
    }
}

