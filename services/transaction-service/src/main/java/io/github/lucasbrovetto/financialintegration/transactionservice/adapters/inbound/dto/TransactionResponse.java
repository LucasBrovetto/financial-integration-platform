package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction Response DTO
 * Record for response payload
 * Excludes null fields to keep JSON response clean
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
        @Schema(description = "Unique transaction identifier", format = "uuid",
                example = "6f9619ff-8b86-d011-b42d-00cf4fc964ff")
        UUID id,

        @Schema(description = "Identifier of the POS terminal", example = "POS-UR-001")
        String terminalId,

        @Schema(description = "Transaction amount", example = "150.50")
        BigDecimal amount,

        @Schema(description = "Financial operation type", example = "SALE",
                allowableValues = {"SALE", "REFUND", "REVERSAL"})
        TransactionType type,

        @Schema(description = "Current transaction lifecycle status", example = "PENDING",
                allowableValues = {"PENDING", "APPROVED", "DECLINED", "REFUNDED", "REVERSED", "PENDING_RETRY"})
        TransactionStatus status,

        @Schema(description = "Creation time in service-local ISO-8601 format",
                example = "2026-07-20T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Last update time in service-local ISO-8601 format",
                example = "2026-07-20T10:30:00")
        LocalDateTime updatedAt,

        @Schema(description = "Failure description when the transaction cannot be completed",
                example = "Acquirer declined the transaction", nullable = true)
        String failureReason
) {
}
