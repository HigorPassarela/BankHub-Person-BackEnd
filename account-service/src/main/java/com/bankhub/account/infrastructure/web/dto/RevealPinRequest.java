package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload exigido para destrancar operações sensíveis (Como ver a senha do cartão).
 */
public record RevealPinRequest(

        @NotBlank(message = "A sua Assinatura Eletrônica (PIN Transacional) é obrigatória.")
        @Pattern(regexp = "^\\d{4}$", message = "O PIN transacional deve conter exatamente 4 números.")
        String transactionPin

) {}