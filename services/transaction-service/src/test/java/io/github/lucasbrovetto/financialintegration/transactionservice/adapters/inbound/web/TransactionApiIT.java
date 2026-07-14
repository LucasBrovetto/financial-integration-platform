package io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.web;

import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.inbound.dto.TransactionResponse;
import io.github.lucasbrovetto.financialintegration.transactionservice.adapters.outbound.persistence.TransactionJpaRepository;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionStatus;
import io.github.lucasbrovetto.financialintegration.transactionservice.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class TransactionApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:17"))
                    .withDatabaseName("financial_platform")
                    .withUsername("postgres")
                    .withPassword("12345");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionJpaRepository.deleteAll();
    }

    @Test
    void createTransaction_persistsAndReturnsItThroughTheApi() throws Exception {
        var createResponse = postJson("/transactions", fixture("fixtures/transactions/create-sale.json"));

        assertEquals(201, createResponse.statusCode());
        var created = objectMapper.readValue(createResponse.body(), TransactionResponse.class);
        assertEquals("POS-UR-001", created.terminalId());
        assertEquals(new BigDecimal("150.50"), created.amount());
        assertEquals(TransactionType.SALE, created.type());
        assertEquals(TransactionStatus.PENDING, created.status());
        assertTrue(transactionJpaRepository.existsById(created.id()));

        var getResponse = get("/transactions/" + created.id());

        assertEquals(200, getResponse.statusCode());
        var retrieved = objectMapper.readValue(getResponse.body(), TransactionResponse.class);
        assertEquals(created.id(), retrieved.id());
        assertEquals(created.terminalId(), retrieved.terminalId());
        assertEquals(created.status(), retrieved.status());
    }

    @Test
    void createTransaction_withInvalidAmountReturnsValidationError() throws Exception {
        var response = postJson("/transactions", fixture("fixtures/transactions/create-sale-invalid-amount.json"));

        assertEquals(400, response.statusCode());
        assertEquals("VALIDATION_ERROR", objectMapper.readTree(response.body()).get("error").asString());
        assertEquals("Amount must be positive", objectMapper.readTree(response.body())
                .get("details").get("amount").asString());
    }

    @Test
    void getTransaction_withUnknownIdReturnsNotFound() throws Exception {
        var response = get("/transactions/" + UUID.randomUUID());

        assertEquals(404, response.statusCode());
        assertEquals("NOT_FOUND", objectMapper.readTree(response.body()).get("error").asString());
    }

    @Test
    void getTransaction_withInvalidIdReturnsBadRequest() throws Exception {
        var response = get("/transactions/not-a-uuid");

        assertEquals(400, response.statusCode());
        assertEquals("INVALID_PATH_PARAMETER", objectMapper.readTree(response.body()).get("error").asString());
    }

    private HttpResponse<String> postJson(String path, String body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(uri(path)).GET().build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String fixture(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
