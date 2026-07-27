package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload HTTP para o processo de Primeiro Acesso via Magic Link (Token Temporário).
 */
public record ActivationRequest(
        @NotBlank(message = "O token de ativação é obrigatório.")
        String token
) {
}