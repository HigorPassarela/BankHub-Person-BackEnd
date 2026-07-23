package com.bankhub.transaction.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionEventMessage(
        @JsonProperty("header") Header header,
        @JsonProperty("payload") Payload payload
) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("timestamp") LocalDateTime timestamp
    ) {}

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("sourceAccountId") String sourceAccountId,
            @JsonProperty("destinationAccountId") String destinationAccountId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("type") String type,
            @JsonProperty("status") String status,

            // Os 3 campos que o account-service usa na ida, ou usa pra mandar erro na volta
            @JsonProperty("accountId") String accountId,
            @JsonProperty("agency") String agency,
            @JsonProperty("accountNumber") String accountNumber
    ) {}
}