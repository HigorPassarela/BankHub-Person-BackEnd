package com.bankhub.account.infrastructure.web.api;

import com.bankhub.account.infrastructure.web.dto.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Operações de Gerenciamento de Contas (Bank-Hub)")
public interface AccountApi {

    @Operation(summary = "Criar uma nova conta bancária para o usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    ResponseEntity<AccountResponse> createAccount(
            @Parameter(description = "ID do usuário injetado pelo API Gateway via JWT", hidden = true)
            @RequestHeader("X-User-Id") String customerId
    );

    @Operation(summary = "Consultar os dados e o saldo de uma conta específica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada ou pertence a outro cliente",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{accountId}")
    ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "ID da conta que deseja consultar")
            @PathVariable String accountId,
            @Parameter(description = "ID do usuário injetado pelo API Gateway via JWT", hidden = true)
            @RequestHeader("X-User-Id") String customerId
    );
}
