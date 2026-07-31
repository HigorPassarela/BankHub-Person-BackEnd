package com.bankhub.investment.infrastructure.web.controller;

import com.bankhub.investment.application.port.in.BuyAssetUseCase;
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
}
