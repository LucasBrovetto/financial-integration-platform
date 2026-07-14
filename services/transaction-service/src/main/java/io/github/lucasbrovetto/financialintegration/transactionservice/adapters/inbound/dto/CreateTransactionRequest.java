package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto;

import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Create Transaction Request DTO
 */
public record CreateTransactionRequest(
        @Schema(description = "Identifier of the POS terminal", example = "POS-UR-001")
        @NotBlank(message = "Terminal ID cannot be blank")
        String terminalId,

        @Schema(description = "Transaction amount", example = "150.50")
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @Schema(description = "Financial operation type", example = "SALE")
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
