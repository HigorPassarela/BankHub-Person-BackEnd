package com.bankhub.account.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Payload do comando recebido do Camunda (Onboarding Service) via Kafka.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OnboardingCommandMessage(
        @JsonProperty("payload") Payload payload
) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(
            @JsonProperty("customerId") String customerId,
            @JsonProperty("fullName") String fullName,
            @JsonProperty("phone") String phone,
            @JsonProperty("address") String address
    ){}
}
