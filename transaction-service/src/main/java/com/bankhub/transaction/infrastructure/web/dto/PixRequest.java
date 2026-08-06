package com.bankhub.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Payload recebido do front-end para iniciar um PIX.
 * Implementado como record para imutabilidade e menor boilerplate.
 * Jackson suporta records nativamente desde a versão 2.12+.
 */
public record PixRequest(
        @NotBlank(message = "O ID da conta de origem é obrigatório.")
        String sourceAccountId,

        @NotBlank(message = "O ID da conta de destino é obrigatório.")
        String destinationAccountId,

        @NotNull(message = "O valor da transação é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor do PIX deve ser maior que zero.")
        BigDecimal amount,

        @NotBlank(message = "A sua Assinatura Eletrônica (PIN Transacional) é obrigatória para realizar o PIX.")
        @Pattern(regexp = "^\\d{4}$", message = "O PIN transacional deve conter exatamente 4 números.")
        String transactionPin,

        String category
) {
}