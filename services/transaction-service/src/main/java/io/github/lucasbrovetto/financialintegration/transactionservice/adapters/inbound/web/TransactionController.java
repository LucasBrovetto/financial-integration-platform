package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.web;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.exception.ErrorResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.mapper.TransactionDtoMapper;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.GetTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Transaction REST Controller
 * Inbound Adapter: Exposes transaction operations via HTTP REST API
 * Translates HTTP requests/responses using DTOs
 * Delegates to application use cases
 */
@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Payment transaction lifecycle operations")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final TransactionDtoMapper transactionDtoMapper;

    /**
     * POST /transactions
     * Create a new transaction
     *
     * @param request The create transaction request
     * @return ResponseEntity with created transaction and 201 status
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "createTransaction", summary = "Create a transaction",
            description = "Creates and persists a new transaction with PENDING status.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid transaction request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:30:00\",\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Request validation failed\",\"path\":\"/transactions\",\"details\":{\"amount\":\"Amount must be positive\"}}"))),
            @ApiResponse(responseCode = "500", description = "Transaction could not be persisted or an unexpected error occurred",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {

        log.info("POST /transactions - Creating transaction for terminal: {}", request.terminalId());

        // Convert DTO to domain command and execute use case
        var command = transactionDtoMapper.toCommand(request);
        Transaction transaction = createTransactionUseCase.execute(command);

        // Convert domain model to response DTO
        TransactionResponse response = transactionDtoMapper.toResponse(transaction);

        log.info("Transaction created successfully: {}", transaction.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /transactions/{transactionId}
     * Retrieve a transaction by ID
     *
     * @param transactionId The transaction identifier
     * @return ResponseEntity with transaction and 200 status
     */
    @GetMapping(value = "/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getTransaction", summary = "Get a transaction",
            description = "Retrieves the latest persisted representation of a transaction by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Transaction identifier is not a UUID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error while retrieving the transaction",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction UUID", required = true,
                    example = "6f9619ff-8b86-d011-b42d-00cf4fc964ff")
            @PathVariable UUID transactionId) {

        log.info("GET /transactions/{} - Retrieving transaction", transactionId);

        Transaction transaction = getTransactionUseCase.execute(transactionId);
        TransactionResponse response = transactionDtoMapper.toResponse(transaction);

        log.info("Transaction retrieved successfully: {}", transactionId);
        return ResponseEntity.ok(response);

    }
}
