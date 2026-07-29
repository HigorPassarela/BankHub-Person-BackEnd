package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PinRequest(

        @NotBlank(message = "O PIN transacional é obrigatório")
        @Pattern(regexp = "^\\d{4}$", message = "O PIN deve conter exatamente 4 números.")
        String transactionPin
) {
}
