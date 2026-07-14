package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.mapper;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import org.mapstruct.Mapper;

/**
 * DTO Mapper for Transaction
 * Converts between domain model and DTOs
 * Using MapStruct for automatic mapping
 *
 */

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Map Transaction domain model to response DTO
     */
    TransactionResponse toResponse(Transaction transaction);

    /**
     * Map CreateTransactionRequest DTO to domain command
     */
    CreateTransactionCommand toCommand(CreateTransactionRequest request);


}

