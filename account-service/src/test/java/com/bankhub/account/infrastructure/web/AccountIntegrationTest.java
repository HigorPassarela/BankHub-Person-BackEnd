package com.bankhub.account.infrastructure.web;

import com.bankhub.account.base.BaseIntegrationTest;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.infrastructure.persistence.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar uma conta com sucesso via API REST")
    void shouldCreateAccountViaApi() {
        given()
            .header("X-User-Id", "customer-integracao")
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/accounts")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("customerId", equalTo("customer-integracao"))
            .body("status", equalTo(AccountStatus.PENDING_ACTIVATION.name()))
            .body("account", notNullValue());
    }
}
