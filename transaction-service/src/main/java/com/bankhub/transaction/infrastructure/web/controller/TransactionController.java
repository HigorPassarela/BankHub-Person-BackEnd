package com.bankhub.transaction.infrastructure.web.controller;

import com.bankhub.transaction.application.port.in.CompletePixUseCase;
import com.bankhub.transaction.application.port.in.GetStatementUseCase;
import com.bankhub.transaction.application.port.in.InitiatePixUseCase;
import com.bankhub.transaction.application.port.in.ResolveBoletoUseCase;
import com.bankhub.transaction.application.port.in.ResolvePixKeyUseCase;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.infrastructure.web.dto.BoletoResolveResponse;
import com.bankhub.transaction.infrastructure.web.dto.PixKeyResolveResponse;
import com.bankhub.transaction.infrastructure.web.dto.PixRequest;
import com.bankhub.transaction.infrastructure.web.dto.PixResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Motor de Pagamentos, Transferências (PIX) e Extratos")
public class TransactionController {

    private final InitiatePixUseCase initiatePixUseCase;
    private final CompletePixUseCase completePixUseCase;
    private final GetStatementUseCase getStatementUseCase;
    private final ResolvePixKeyUseCase resolvePixKeyUseCase;
    private final ResolveBoletoUseCase resolveBoletoUseCase;

    @PostMapping("/pix")
    @Operation(summary = "Inicia uma transferência PIX entre contas", description = "Valida saldo, PIN e inicia a saga de transferência.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "PIX Aceito e em processamento"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "PIN incorreto ou bloqueio de segurança"),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente")
    })
    public ResponseEntity<PixResponse> initiatePix(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody PixRequest request) {

        log.info("Recebida requisição REST para iniciar PIX. Origem: {}", request.sourceAccountId());

        Transaction transaction = initiatePixUseCase.execute(
                customerId, request.sourceAccountId(), request.destinationAccountId(), request.amount(),
                request.transactionPin(), request.category()
        );

        PixResponse response = new PixResponse(transaction.id(), transaction.status().name(), "Em processamento");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{accountId}/statement")
    @Operation(summary = "Retorna o extrato bancário detalhado de uma conta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<List<Transaction>> getStatement(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String customerId,
            @PathVariable String accountId) {
        
        log.info("Recebida requisição REST de Extrato. Conta: {}", accountId);
        return ResponseEntity.ok(getStatementUseCase.execute(accountId, customerId));
    }

    @PostMapping("/internal/ledger")
    @Operation(summary = "Endpoint interno (M2M) para registro direto no Ledger (Ex: Home Broker)")
    public ResponseEntity<Void> registerLedger(@RequestBody Map<String, Object> request) {
        log.info("Recebida requisição interna M2M para gravar no Ledger.");
        return ResponseEntity.ok().build();
    }
}
