package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.AdjustCardLimitUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdjustCardLimitService implements AdjustCardLimitUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final CardPersistencePort cardPersistencePort;

    @Override
    @Transactional
    public Card execute(String accountId, String cardId, String customerId, BigDecimal newLimit) {
        log.info("Iniciando ajuste de limite para R$ {} no cartão: {}. Cliente: {}", newLimit, cardId, customerId);

        accountPersistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        Card card = cardPersistencePort.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado ou não pertence a esta conta."));

        Card adjustedCard = card.adjustLimit(newLimit);

        Card savedCard = cardPersistencePort.save(adjustedCard);

        log.info("Limite do cartão {} ajustado com sucesso para R$ {}.", savedCard.id(), savedCard.creditLimit());

        return savedCard;
    }
}
