package com.financialintegration.transactionservice.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Transaction Status Enum
 * Represents the status of a financial transaction in its lifecycle.
 */
@Getter
@RequiredArgsConstructor
public enum TransactionStatus {

    PENDING("Transaction is pending authorization"),
    APPROVED("Transaction has been approved"),
    DECLINED("Transaction has been declined"),
    REFUNDED("Transaction has been refunded"),
    REVERSED("Transaction has been reversed"),
    PENDING_RETRY("Transaction is pending retry after error");

    private final String description;

    public boolean isTerminal() {
        return switch (this) {
            case APPROVED, DECLINED, REFUNDED, REVERSED -> true;
            default -> false;
        };

    }
}

