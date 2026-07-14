package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.web;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.CreateTransactionRequest;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.exception.GlobalExceptionHandler;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.mapper.TransactionMapper;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionCommand;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.CreateTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.application.port.in.GetTransactionUseCase;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.exception.TransactionNotFoundException;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.Transaction;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockitoBean
    private GetTransactionUseCase getTransactionUseCase;

    @MockitoBean
    private TransactionMapper transactionMapper;

    @Test
    @DisplayName("POST /transactions - success returns 201 and created payload")
    void createTransaction_success() throws Exception {
        var request = new CreateTransactionRequest(
                "terminal-123",
                BigDecimal.valueOf(100),
                TransactionType.SALE
        );

        var command = new CreateTransactionCommand(
                request.terminalId(), request.amount(), request.type()
        );

        var transactionId = UUID.randomUUID();
        var now = LocalDateTime.now();

        var created = Transaction.restore(
                transactionId, request.terminalId(), request.amount(), request.type(),
                TransactionStatus.PENDING, now, now, null
        );

        var response = new TransactionResponse(
                transactionId, request.terminalId(), request.amount(), request.type(),
                TransactionStatus.PENDING, now, now, null
        );

        when(transactionMapper.toCommand(request)).thenReturn(command);
        when(createTransactionUseCase.execute(command)).thenReturn(created);
        when(transactionMapper.toResponse(created)).thenReturn(response);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.terminalId").value("terminal-123"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.type").value("SALE"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(transactionMapper).toCommand(request);
        verify(createTransactionUseCase).execute(command);
        verify(transactionMapper).toResponse(created);
    }

    @Test
    @DisplayName("POST /transactions - validation error returns 400 with details")
    void createTransaction_validationError() throws Exception {
        // negative amount should trigger validation error
        var request = new CreateTransactionRequest(
                "term-1",
                BigDecimal.valueOf(-10),
                TransactionType.SALE
        );

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.amount").exists());

        verifyNoInteractions(createTransactionUseCase);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("POST /transactions - unexpected error returns 500")
    void createTransaction_unexpectedError() throws Exception {
        var request = new CreateTransactionRequest(
                "term-1",
                BigDecimal.valueOf(100),
                TransactionType.SALE
        );

        var command = new CreateTransactionCommand(
                request.terminalId(),
                request.amount(),
                request.type()
        );

        when(transactionMapper.toCommand(request)).thenReturn(command);
        when(createTransactionUseCase.execute(command))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

        verify(transactionMapper).toCommand(request);
        verify(createTransactionUseCase).execute(command);
    }

    @Test
    @DisplayName("GET /transactions/{id} - success returns 200 and payload")
    void getTransaction_success() throws Exception {
        var transactionId = UUID.randomUUID();

        var found = Transaction.restore(
                transactionId, "term-1", BigDecimal.valueOf(55), TransactionType.SALE,
                TransactionStatus.PENDING, LocalDateTime.now(), LocalDateTime.now(), null
        );

        var response = new TransactionResponse(
                transactionId, "term-1", BigDecimal.valueOf(55), TransactionType.SALE, TransactionStatus.PENDING,
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(getTransactionUseCase.execute(transactionId)).thenReturn(found);
        when(transactionMapper.toResponse(found)).thenReturn(response);

        mockMvc.perform(get("/transactions/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.terminalId").value("term-1"))
                .andExpect(jsonPath("$.amount").value(55))
                .andExpect(jsonPath("$.type").value("SALE"))
                .andExpect(jsonPath("$.status").value("PENDING"));;

        verify(getTransactionUseCase).execute(transactionId);
        verify(transactionMapper).toResponse(found);
    }

    @Test
    @DisplayName("GET /transactions/{id} - not found returns 404 error")
    void getTransaction_notFound() throws Exception {
        var id = UUID.randomUUID();

        when(getTransactionUseCase.execute(id)).thenThrow(new TransactionNotFoundException(id));

        mockMvc.perform(get("/transactions/{transactionId}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());

        verify(getTransactionUseCase).execute(id);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("GET /transactions/{id} - invalid UUID returns 400")
    void getTransaction_invalidUuid() throws Exception {
        mockMvc.perform(get("/transactions/{transactionId}", "WRONG_UUID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PATH_PARAMETER"))
                .andExpect(jsonPath("$.message")
                        .value("Transaction ID must be a valid UUID"));

        verifyNoInteractions(getTransactionUseCase);
        verifyNoInteractions(transactionMapper);
    }

}

