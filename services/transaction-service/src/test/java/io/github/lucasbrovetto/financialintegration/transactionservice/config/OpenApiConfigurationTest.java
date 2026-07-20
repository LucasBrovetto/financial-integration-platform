package io.github.lucasbrovetto.financialintegration.transactionservice.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigurationTest {

    @Test
    void transactionServiceOpenApi_exposesServiceMetadata() {
        var openApi = new OpenApiConfiguration().transactionServiceOpenApi();

        assertEquals("Transaction Service API", openApi.getInfo().getTitle());
        assertEquals("1.0.0", openApi.getInfo().getVersion());
        assertEquals("Stable Sprint 1 contract for creating and retrieving payment transactions. "
                + "New transactions are returned with PENDING status.", openApi.getInfo().getDescription());
    }
}
