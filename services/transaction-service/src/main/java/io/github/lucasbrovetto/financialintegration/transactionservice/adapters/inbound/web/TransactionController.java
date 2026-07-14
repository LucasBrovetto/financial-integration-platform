package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.web;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.mapper.TransactionMapper;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.GetTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final TransactionMapper transactionMapper;

    /**
     * POST /transactions
     * Create a new transaction
     *
     * @param request The create transaction request
     * @return ResponseEntity with created transaction and 201 status
     */
    @PostMapping
    @Operation(summary = "Create a transaction", description = "Creates a new transaction with PENDING status.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transaction request")
    })
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {

        log.info("POST /transactions - Creating transaction for terminal: {}", request.terminalId());

        // Convert DTO to domain command and execute use case
        var command = transactionMapper.toCommand(request);
        Transaction transaction = createTransactionUseCase.execute(command);

        // Convert domain model to response DTO
        TransactionResponse response = transactionMapper.toResponse(transaction);

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
    @GetMapping("/{transactionId}")
    @Operation(summary = "Get a transaction", description = "Retrieves a transaction by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transaction identifier"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId) {

        log.info("GET /transactions/{} - Retrieving transaction", transactionId);

        Transaction transaction = getTransactionUseCase.execute(transactionId);
        TransactionResponse response = transactionMapper.toResponse(transaction);

        log.info("Transaction retrieved successfully: {}", transactionId);
        return ResponseEntity.ok(response);

    }
}
