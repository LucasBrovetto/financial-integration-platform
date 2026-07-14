package io.github.lucasbrovetto.financialintegration.transactionservice.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Transaction Type - Pure domain enum
 * Represents the type of financial transaction.
 */
@Getter
@RequiredArgsConstructor
public enum TransactionType {
    SALE("Sale transaction"),
    REFUND("Refund transaction"),
    REVERSAL("Reversal transaction");

    private final String description;

}

