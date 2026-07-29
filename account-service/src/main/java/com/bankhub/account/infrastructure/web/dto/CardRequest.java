package com.bankhub.account.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CardRequest(

        @NotBlank(message = "O tipo de cartão é obrigatório (PHYSICAL, VIRTUAL, TEMPORARY).")
        String type,

        // Apenas para PHYSICAL, pode vir nulo nos outros.
        String physicalPin
) {
}
