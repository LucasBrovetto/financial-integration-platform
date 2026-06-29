package com.financialintegration.transactionservice.adapters.inbound.mapper;

import com.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import com.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import com.financialintegration.transactionservice.adapters.outbound.persistence.TransactionEntity;
import com.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import com.financialintegration.transactionservice.domain.model.Transaction;
import org.mapstruct.Mapper;

/**
 * DTO Mapper for Transaction
 * Converts between domain model and DTOs
 * Using MapStruct for automatic mapping
 * */

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

