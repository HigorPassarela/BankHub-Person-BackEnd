package com.bankhub.investment.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Payload para a emissão de uma Ordem de Compra.
 */
public record BuyAssetRequest(

        @NotBlank(message = "O ID da Conta Corrente (para o débito) é obrigatório.")
        String accountId,

        @NotBlank(message = "O Ticker (Código do Ativo) é obrigatório.")
        String ticker,

        @NotBlank(message = "O Tipo do Ativo (ex: STOCK, CDB, FII) é obrigatório.")
        String type,

        @NotNull(message = "A quantidade de cotas é obrigatória.")
        @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero.")
        BigDecimal quantity
) {
}
