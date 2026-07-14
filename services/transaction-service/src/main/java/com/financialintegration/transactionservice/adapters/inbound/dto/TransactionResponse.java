package com.financialintegration.transactionservice.adapters.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.financialintegration.transactionservice.domain.model.TransactionStatus;
import com.financialintegration.transactionservice.domain.model.TransactionType;

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
        UUID id,
        String terminalId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String failureReason
) {
}

