package com.bankhub.transaction.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record SagaReplyMessage(
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
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("sagaStatus") String sagaStatus,
            @JsonProperty("failureReason") String failureReason
    ) {
    }
}
