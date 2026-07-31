package com.bankhub.investment.infrastructure.web.controller;

import com.bankhub.investment.application.port.in.BuyAssetUseCase;
import com.bankhub.investment.application.port.in.GetPortfolioUseCase;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.web.dto.BuyAssetRequest;
import com.bankhub.investment.infrastructure.web.dto.PortfolioResponse;
import com.bankhub.investment.infrastructure.web.mapper.PortfolioWebMapper;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Tag(name = "Investment", description = "Home Broker e Gerenciamento de Carteiras (Bank-Hub)")
public class InvestmentController {

    private final BuyAssetUseCase buyAssetUseCase;
    private final GetPortfolioUseCase getPortfolioUseCase;
    private final PortfolioWebMapper webMapper;

    @PostMapping("/buy")
    @Operation(summary = "Emite uma ordem de compra para um ativo financeiro (Ações, CDBs).")
    public ResponseEntity<PortfolioResponse> buyAsset(
            @Parameter(description = "ID do usuário (Segurança)", hidden = true)
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody BuyAssetRequest request) {

        log.info("Recebida requisição REST de Compra de Ativo. Cliente: {}, Ticker: {}", customerId, request.ticker());

        Portfolio portfolio = buyAssetUseCase.execute(
                customerId,
                request.accountId(),
                request.ticker(),
                request.type(),
                request.quantity(),
                request.transactionPin()
        );

        PortfolioResponse response = webMapper.toResponse(portfolio);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/portfolio/{customerId}")
    @Operation(summary = "Retorna a carteira consolidada de ativos de um cliente.")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @Parameter(description = "ID do usuário logado", hidden = true)
            @RequestHeader("X-User-Id") String headerCustomerId,

            @Parameter(description = "ID do cliente a ser consultado")
            @PathVariable String customerId) {

        log.info("Recebida requisição REST de Consulta de Portfólio. Solicitante: {}, Alvo: {}", headerCustomerId, customerId);

        if (!headerCustomerId.equals(customerId)) {
            log.warn("Acesso Negado: Usuário {} tentou consultar carteira do cliente {}.", headerCustomerId, customerId);
            throw new SecurityException("Você não tem permissão para acessar a carteira de outro cliente.");
        }

        Portfolio portfolio = getPortfolioUseCase.execute(customerId);

        PortfolioResponse response = webMapper.toResponse(portfolio);

        return ResponseEntity.ok(response);
    }
}
