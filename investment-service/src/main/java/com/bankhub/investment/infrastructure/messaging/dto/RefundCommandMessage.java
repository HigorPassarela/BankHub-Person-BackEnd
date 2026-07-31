package com.bankhub.investment.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payload do comando enviado ao Kafka solicitando o estorno assíncrono de valores.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record RefundCommandMessage(
        @JsonProperty("header") Header header,
        @JsonProperty("payload") Payload payload
) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("timestamp") LocalDateTime timestamp
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            @JsonProperty("accountId") String accountId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("amount") BigDecimal amount
    ) {
    }
}
