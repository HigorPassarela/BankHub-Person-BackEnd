package com.bankhub.account.application.port.out;

import com.bankhub.account.domain.Card;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para operações de persistência do agregado Cartões (Cards).
 */
public interface CardPersistencePort {

    Card save(Card card);

    List<Card> findAllByAccountId(String accountId);

    Optional<Card> findByIdAndAccountId(String cardId, String accountId);

    void deleteById(String cardId);
}
