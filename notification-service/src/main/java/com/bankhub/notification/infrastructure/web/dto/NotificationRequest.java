package com.bankhub.notification.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank(message = "O ID da conta é obrigatório") String accountId,
        @NotBlank(message = "O tipo de evento é obrigatório") String eventType,
        @NotBlank(message = "O status da conta é obrigatório") String status,
        @NotBlank(message = "A agência é obrigatória") String agency,
        @NotBlank(message = "O número da conta é obrigatório") String accountNumber,
        String activationToken
) {}
