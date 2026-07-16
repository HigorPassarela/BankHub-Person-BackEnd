package com.bankhub.hubia.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Representa o corpo da requisição enviada pelo cliente.
 */
public record ChatRequest(
        @NotBlank(message = "A mensagem não pode ser vazia.")
        String message
) {
}
