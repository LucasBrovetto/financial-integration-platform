package io.github.lucasbrovetto.financialintegration.transactionservice.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigurationTest {

    @Test
    void transactionServiceOpenApi_exposesServiceMetadata() {
        var openApi = new OpenApiConfiguration().transactionServiceOpenApi();

        assertEquals("Transaction Service API", openApi.getInfo().getTitle());
        assertEquals("v1", openApi.getInfo().getVersion());
    }
}
