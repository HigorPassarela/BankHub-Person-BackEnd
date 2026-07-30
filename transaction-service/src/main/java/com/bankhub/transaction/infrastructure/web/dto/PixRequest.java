package com.bankhub.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload recebido do front-end para iniciar um PIX.
 * OBS: Usando Class padrão (em vez de Record) para garantir a desserialização do Jackson.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PixRequest {

    @NotBlank(message = "O ID da conta de destino é obrigatório.")
    private String destinationAccountId;

    @NotNull(message = "O valor da transação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor do PIX deve ser maior que zero.")
    private BigDecimal amount;

    @NotBlank(message = "A sua Assinatura Eletrônica (PIN Transacional) é obrigatória para realizar o PIX.")
    @Pattern(regexp = "^\\d{4}$", message = "O PIN transacional deve conter exatamente 4 números.")
    private String transactionPin;

    private String category;
}