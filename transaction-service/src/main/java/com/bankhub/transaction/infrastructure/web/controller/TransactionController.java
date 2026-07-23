package com.bankhub.transaction.infrastructure.web.controller;

import com.bankhub.transaction.application.port.in.InitiatePixUseCase;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.web.dto.PixRequest;
import com.bankhub.transaction.infrastructure.web.dto.PixResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Motor de Transferências e PIX (Bank-Hub)")
public class TransactionController {

    private final InitiatePixUseCase initiatePixUseCase;

    @PostMapping("/pix")
    @Operation(summary = "Inicia uma transferência PIX entre contas.")
    public ResponseEntity<PixResponse> initiatePix(
            @Parameter(description = "ID do usuário remetente", hidden = true)
            @RequestHeader("X-User-Id") String customerId,

            @Valid @RequestBody PixRequest request) {

        log.info("Recebida requisição REST de PIX. Solicitante (User): {}, Destino (Conta): {}, Valor: {}",
                customerId, request.getDestinationAccountId(), request.getAmount());

        Transaction transaction = initiatePixUseCase.execute(
                customerId,
                request.getDestinationAccountId(),
                request.getAmount()
        );

        PixResponse response = PixResponse.builder()
                .transactionId(transaction.id())
                .destinationAccountId(transaction.destinationAccountId())
                .amount(transaction.amount())
                .status(transaction.status().name())
                .timestamp(transaction.createdAt())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
