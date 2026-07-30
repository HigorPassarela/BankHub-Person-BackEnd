package com.bankhub.account.infrastructure.web.controller;

import com.bankhub.account.application.port.in.AdjustCardLimitUseCase;
import com.bankhub.account.application.port.in.BlockCardUseCase;
import com.bankhub.account.application.port.in.GenerateCardUseCase;
import com.bankhub.account.application.port.in.RevealCardPinUseCase;
import com.bankhub.account.domain.Card;
import com.bankhub.account.infrastructure.web.dto.AdjustLimitRequest;
import com.bankhub.account.infrastructure.web.dto.CardRequest;
import com.bankhub.account.infrastructure.web.dto.CardResponse;
import com.bankhub.account.infrastructure.web.dto.RevealPinRequest;
import com.bankhub.account.infrastructure.web.mapper.CardWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts/{accountId}/cards")
@RequiredArgsConstructor
@Tag(name = "Cards", description = "Operações de Emissão e Gestão de Cartões de Crédito (Bank-Hub)")
public class CardController {

    private final GenerateCardUseCase generateCardUseCase;
    private final RevealCardPinUseCase revealCardPinUseCase;
    private final BlockCardUseCase blockCardUseCase;
    private final AdjustCardLimitUseCase adjustCardLimitUseCase;
    private final CardWebMapper webMapper;

    @PostMapping
    @Operation(summary = "Emite um novo cartão de crédito (Físico, Virtual ou Temporário).")
    public ResponseEntity<CardResponse> generateCard(
            @PathVariable String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody CardRequest request) {

        log.info("Recebida requisição REST de Emissão de Cartão. Tipo: {}, Conta: {}", request.type(), accountId);

        Card generatedCard = generateCardUseCase.execute(accountId, customerId, request.type(), request.physicalPin());

        CardResponse response = webMapper.toResponse(generatedCard);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{cardId}/reveal-pin")
    @Operation(summary = "Revela a senha do cartão físico após validação do PIN transacional de 4 dígitos.")
    public ResponseEntity<Map<String, String>> revealCardPin(
            @PathVariable String accountId,
            @PathVariable String cardId,
            @Parameter(description = "ID do usuário (Segurança Zero Trust)", hidden = true)
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody RevealPinRequest request) {

        log.info("Recebida requisição REST de Reveal PIN para o cartão: {}", cardId);

        String clearPin = revealCardPinUseCase.execute(accountId, cardId, customerId, request.transactionPin());

        return ResponseEntity.ok(Map.of("pin", clearPin));
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Alterna o status de bloqueio de um cartão (Bloquear/Desbloquear).")
    public ResponseEntity<CardResponse> toggleCardBlock(
            @PathVariable String accountId,
            @PathVariable String cardId,
            @Parameter(description = "ID do usuário (Segurança Zero Trust)", hidden = true)
            @RequestHeader("X-User-Id") String customerId) {

        log.info("Recebida requisição REST de (Des)Bloqueio para o cartão: {}", cardId);

        Card toggledCard = blockCardUseCase.execute(accountId, cardId, customerId);
        CardResponse response = webMapper.toResponse(toggledCard);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/limit")
    @Operation(summary = "Ajusta o limite de crédito estabelecido para um cartão.")
    public ResponseEntity<CardResponse> adjustCardLimit(
            @PathVariable String accountId,
            @PathVariable String cardId,
            @Parameter(description = "ID do usuário (Segurança Zero Trust)", hidden = true)
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody AdjustLimitRequest request) {

        log.info("Recebida requisição REST de Ajuste de Limite para o cartão: {}", cardId);

        Card adjustedCard = adjustCardLimitUseCase.execute(accountId, cardId, customerId, request.newLimit());
        CardResponse response = webMapper.toResponse(adjustedCard);

        return ResponseEntity.ok(response);
    }
}
