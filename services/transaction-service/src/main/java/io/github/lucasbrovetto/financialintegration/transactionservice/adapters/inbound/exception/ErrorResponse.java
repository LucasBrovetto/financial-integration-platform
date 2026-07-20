package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error Response DTO
 * Standard structure for all error responses
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Standard error returned by every transaction endpoint")
public class ErrorResponse {
    @Schema(description = "Time when the error was produced in service-local ISO-8601 format",
            example = "2026-07-20T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Stable machine-readable error code", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error description", example = "Request validation failed")
    private String message;

    @Schema(description = "Request path that produced the error", example = "/transactions")
    private String path;

    @Schema(description = "Validation messages keyed by request field",
            example = "{\"amount\": \"Amount must be positive\"}")
    private Map<String, String> details;
}
