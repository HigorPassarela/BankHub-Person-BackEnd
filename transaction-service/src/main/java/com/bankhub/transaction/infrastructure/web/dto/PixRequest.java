package com.bankhub.transaction.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

}