package com.bankhub.onboarding.infrastructure.web.controller;

import com.bankhub.onboarding.application.port.in.StartOnboardingUseCase;
import com.bankhub.onboarding.infrastructure.web.dto.OnboardingRequest;
import com.bankhub.onboarding.infrastructure.web.dto.OnboardingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "Operações de solicitação de abertura de contas orquestradas pelo Camunda 8")
public class OnboardingController {

    private final StartOnboardingUseCase startOnboardingUseCase;

    @PostMapping
    @Operation(summary = "Inicia o processo assíncrono de análise de risco para um visitante.")
    public ResponseEntity<OnboardingResponse> startOnboarding(@Valid @RequestBody OnboardingRequest request) {

        log.info("Recebida requisição REST de Visitante. Nome: {}, Documento: {}", request.fullName(), request.documentNumber());

        String protocolNumber = startOnboardingUseCase.execute(
                request.documentNumber(),
                request.monthlyIncome(),
                request.fullName(),
                request.phone(),
                request.address()
        );

        OnboardingResponse response = OnboardingResponse.builder()
                .message("Processamento iniciado.")
                .protocol(protocolNumber)
                .build();

        return ResponseEntity.accepted().body(new OnboardingResponse("Processamento iniciado.", protocolNumber));
    }
}
