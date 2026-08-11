package com.bankhub.transaction.base;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require full Spring context and external dependencies.
 * Uses Testcontainers to provide isolated MongoDB instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:7.0")
    ).withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // MongoDB configuration
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

        // Disable security for tests
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "");
    }

    /**
     * Returns the base URL for REST API calls in tests.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }
}
