package com.financialintegration.transactionservice.adapters.inbound.dto;

import com.financialintegration.transactionservice.domain.model.TransactionStatus;
import com.financialintegration.transactionservice.domain.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Response DTO
 * Record for response payload
 * Excludes null fields to keep JSON response clean
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(
        String id,
        String terminalId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String failureReason
) {
}

