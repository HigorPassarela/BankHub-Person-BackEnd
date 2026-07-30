package com.bankhub.account.infrastructure.web.api;

import com.bankhub.account.infrastructure.web.dto.AccountDictResponse;
import com.bankhub.account.infrastructure.web.dto.AccountResponse;
import com.bankhub.account.infrastructure.web.dto.ActivationRequest;
import com.bankhub.account.infrastructure.web.dto.DebitRequest;
import com.bankhub.account.infrastructure.web.dto.DepositRequest;
import com.bankhub.account.infrastructure.web.dto.PinRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Operações de Gerenciamento de Contas e Ciclo de Vida (Bank-Hub)")
public interface AccountApi {

    @Operation(summary = "Criar uma nova conta bancária para o usuário autenticado (Nasce PENDENTE).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "ID do usuário autenticado (Injetado via API Gateway)")
            @RequestHeader("X-User-Id") String customerId
    );

    @Operation(summary = "Consultar os dados e o saldo de uma conta específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou pertence a outro cliente",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{accountId}")
    ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "ID da conta que deseja consultar")
            @PathVariable String accountId,
            @Parameter(description = "ID do usuário autenticado (Injetado via API Gateway)")
            @RequestHeader("X-User-Id") String customerId
    );

    @Operation(summary = "Ativa uma conta pendente validando o Token Temporário (Magic Link).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta ativada com sucesso. Retorna Agência e Conta.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Token de ativação inválido, expirado ou não encontrado.",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "A conta já está ativa ou bloqueada.",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/activate")
    ResponseEntity<AccountResponse> activateAccount(
            @RequestBody ActivationRequest request
    );

    @Operation(summary = "Realiza um depósito (Cash-In) em uma conta ativa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso (Retorna o novo saldo)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou acesso negado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação (ex: conta não está ativa, valor negativo)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{accountId}/deposit")
    ResponseEntity<AccountResponse> depositAccount(
            @Parameter(description = "ID da conta que receberá o depósito")
            @PathVariable String accountId,
            @Parameter(description = "ID do usuário autenticado (Injetado via API Gateway)")
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody DepositRequest request
    );

    @PostMapping("/{accountId}/debit")
    ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody DebitRequest request
    );

    @Operation(summary = "Cadastra ou altera o PIN Transacional (4 dígitos) de uma conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PIN cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou acesso negado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "O PIN informado não atende aos requisitos (4 dígitos)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{accountId}/pin")
    ResponseEntity<AccountResponse> createTransactionPin(
            @Parameter(description = "ID da conta")
            @PathVariable String accountId,
            @Parameter(description = "ID do usuário (Segurança Zero Trust)")
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody PinRequest request
    );

    @Operation(summary = "Realiza o upload da selfie do usuário para aprovação do KYC.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "KYC aprovado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou ausente",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/{accountId}/kyc/selfie", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AccountResponse> uploadSelfie(
            @Parameter(description = "ID da conta")
            @PathVariable String accountId,
            @Parameter(description = "ID do usuário (Segurança Zero Trust)")
            @RequestHeader("X-User-Id") String customerId,
            @Parameter(description = "Arquivo de imagem (JPEG/PNG)")
            @RequestPart("file") MultipartFile file
    );

    @Operation(summary = "[M2M] Resolve os metadados de uma conta baseada no número (DICT do PIX).")
    @GetMapping("/dict/{accountNumber}")
    ResponseEntity<AccountDictResponse> resolveDict(
            @PathVariable String accountNumber
    );
}