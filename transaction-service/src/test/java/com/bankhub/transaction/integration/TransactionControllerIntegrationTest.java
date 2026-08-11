package com.bankhub.transaction.integration;

import com.bankhub.transaction.base.BaseIntegrationTest;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.infrastructure.persistence.entity.TransactionDocument;
import com.bankhub.transaction.infrastructure.persistence.repository.TransactionRepository;
import com.bankhub.transaction.infrastructure.web.dto.PixRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.bankhub.transaction.infrastructure.client.AccountFeignClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@Disabled("Integration tests require Docker - run separately")
@DisplayName("TransactionController Integration Tests")
class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private AccountFeignClient accountFeignClient;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        transactionRepository.deleteAll();

        // Mock account service validation to always succeed
        doNothing().when(accountFeignClient).validateTransaction(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("should initiate PIX transfer successfully via POST /api/v1/transactions/pix")
    void shouldInitiatePixTransferSuccessfully() {
        // Arrange
        PixRequest request = new PixRequest(
                "acc-source",
                "acc-destination",
                new BigDecimal("100.00"),
                "1234",
                "TRANSFER"
        );

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(getBaseUrl() + "/transactions/pix")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("sourceAccountId", equalTo("acc-source"))
                .body("destinationAccountId", equalTo("acc-destination"))
                .body("amount", equalTo(100.00f))
                .body("status", equalTo("PENDING"))
                .body("category", equalTo("TRANSFER"));
    }

    @Test
    @DisplayName("should return 400 when initiating PIX to same account")
    void shouldReturn400WhenPixToSameAccount() {
        // Arrange
        PixRequest request = new PixRequest(
                "acc-same",
                "acc-same",
                new BigDecimal("100.00"),
                "1234",
                "TRANSFER"
        );

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(getBaseUrl() + "/transactions/pix")
                .then()
                .statusCode(400)
                .body("message", containsString("Não é possível realizar uma transferência para a própria conta"));
    }

    @Test
    @DisplayName("should retrieve statement successfully via GET /api/v1/transactions/statement/{accountId}")
    void shouldRetrieveStatementSuccessfully() {
        // Arrange
        String accountId = "acc-123";
        createTransactionInDatabase("txn-1", accountId, "acc-dest-1", new BigDecimal("50.00"));
        createTransactionInDatabase("txn-2", accountId, "acc-dest-2", new BigDecimal("75.00"));

        // Act & Assert
        given()
                .when()
                .get(getBaseUrl() + "/transactions/statement/" + accountId)
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].sourceAccountId", equalTo(accountId))
                .body("[1].sourceAccountId", equalTo(accountId));
    }

    @Test
    @DisplayName("should return empty statement when no transactions exist")
    void shouldReturnEmptyStatementWhenNoTransactions() {
        // Arrange
        String accountId = "acc-empty";

        // Act & Assert
        given()
                .when()
                .get(getBaseUrl() + "/transactions/statement/" + accountId)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("should resolve boleto successfully via POST /api/v1/transactions/boleto/resolve")
    void shouldResolveBoletoSuccessfully() {
        // Arrange
        String barcode = "84600000000012345678901234567890";

        // Act & Assert
        given()
                .queryParam("barcode", barcode)
                .when()
                .post(getBaseUrl() + "/transactions/boleto/resolve")
                .then()
                .statusCode(200)
                .body("barcode", equalTo(barcode))
                .body("companyName", equalTo("Companhia de Energia Elétrica (Light/Enel)"))
                .body("amount", notNullValue())
                .body("dueDate", notNullValue())
                .body("isExpired", equalTo(false));
    }

    @Test
    @DisplayName("should return 400 when resolving invalid boleto barcode")
    void shouldReturn400WhenResolvingInvalidBoleto() {
        // Arrange
        String invalidBarcode = "ABC123";

        // Act & Assert
        given()
                .queryParam("barcode", invalidBarcode)
                .when()
                .post(getBaseUrl() + "/transactions/boleto/resolve")
                .then()
                .statusCode(400)
                .body("message", containsString("Código de barras inválido"));
    }

    private void createTransactionInDatabase(String id, String sourceAccountId, String destAccountId, BigDecimal amount) {
        TransactionDocument doc = new TransactionDocument();
        doc.setId(id);
        doc.setSourceAccountId(sourceAccountId);
        doc.setDestinationAccountId(destAccountId);
        doc.setAmount(amount);
        doc.setType(com.bankhub.transaction.domain.TransactionType.INTERNAL_TRANSFER);
        doc.setStatus(TransactionStatus.COMPLETED);
        doc.setCategory(com.bankhub.transaction.domain.TransactionCategory.TRANSFER);
        doc.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(doc);
    }
}
