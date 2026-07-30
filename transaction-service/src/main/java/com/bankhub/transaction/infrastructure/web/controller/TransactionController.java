package com.bankhub.transaction.infrastructure.web.controller;

import com.bankhub.transaction.application.port.in.GetStatementUseCase;
import com.bankhub.transaction.application.port.in.InitiatePixUseCase;
import com.bankhub.transaction.application.port.in.ResolveBoletoUseCase;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.web.dto.BoletoResolveResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Motor de Transferências e PIX (Bank-Hub)")
public class TransactionController {

    private final InitiatePixUseCase initiatePixUseCase;
    private final ResolveBoletoUseCase resolveBoletoUseCase;
    private final GetStatementUseCase getStatementUseCase;

    @PostMapping("/pix")
    @Operation(summary = "Inicia uma transferência PIX entre contas.")
    public ResponseEntity<PixResponse> initiatePix(
            @Parameter(description = "ID do usuário remetente", hidden = true)
            @RequestHeader("X-User-Id") String customerId,
            @Parameter(description = "JWT Token", hidden = true)
            @RequestHeader("Authorization") String jwtToken,
            @Valid @RequestBody PixRequest request) {

        log.info("Recebida requisição REST de PIX. Solicitante (User): {}, Destino (Conta): {}, Valor: {}",
                customerId, request.getDestinationAccountId(), request.getAmount());

        Transaction transaction = initiatePixUseCase.execute(
                customerId,
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getTransactionPin(),
                jwtToken,
                request.getCategory()
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

    @GetMapping("/boleto/{barcode}/resolve")
    @Operation(summary = "Consulta os dados de um boleto antes de realizar o pagamento.")
    public ResponseEntity<BoletoResolveResponse> resolveBoleto(
            @Parameter(description = "ID do usuário (Segurança Zero Trust)", hidden = true)
            @RequestHeader("X-User-Id") String customerId,

            @Parameter(description = "Código de Barras ou Linha Digitável (Mín. 10 dígitos)")
            @PathVariable String barcode) {

        log.info("Recebida requisição REST de Consulta de Boleto. Solicitante: {}", customerId);

        BoletoResolveResponse response = resolveBoletoUseCase.execute(barcode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statement")
    @Operation(summary = "Retorna o extrato de transações do usuário logado.")
    public ResponseEntity<List<Map<String, Object>>> getStatement(
            @Parameter(description = "ID do usuário", hidden = true)
            @RequestHeader("X-User-Id") String accountId) {

        log.info("Recebida requisição REST para consulta de extrato. Conta: {}", accountId);

        List<Transaction> transactions = getStatementUseCase.execute(accountId);

        List<Map<String, Object>> response = transactions.stream().map(t -> Map.<String, Object>of(
                "transactionId", t.id(),
                "type", t.sourceAccountId().equals(accountId) ? "DEBIT" : "CREDIT",
                "amount", t.amount(),
                "category", t.category().name(),
                "status", t.status().name(),
                "otherPartyAccount", t.sourceAccountId().equals(accountId) ? t.destinationAccountId() : t.sourceAccountId(),
                "timestamp", t.createdAt()
        )).toList();

        return ResponseEntity.ok(response);
    }
}
