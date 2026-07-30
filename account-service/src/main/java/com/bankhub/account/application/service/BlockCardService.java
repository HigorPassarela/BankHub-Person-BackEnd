package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.BlockCardUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockCardService implements BlockCardUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final CardPersistencePort cardPersistencePort;

    @Override
    @Transactional
    public Card execute(String accountId, String cardId, String customerId) {
        log.info("Iniciando ação de (des)bloqueio para o cartão: {}. Solicitante: {}", cardId, customerId);

        accountPersistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        Card card = cardPersistencePort.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado ou não pertence a esta conta."));

        Card toggledCard = card.toggleBlock();

        Card savedCard = cardPersistencePort.save(toggledCard);

        log.info("Status do cartão {} alterado com sucesso. Bloqueado: {}", savedCard.id(), savedCard.isBlocked());

        return savedCard;
    }
}
