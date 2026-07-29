package com.bankhub.account.infrastructure.adapter.out.persistence;

import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.domain.Card;
import com.bankhub.account.infrastructure.mapper.CardMapper;
import com.bankhub.account.infrastructure.persistence.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardPersistenceAdapter implements CardPersistencePort {

    private final CardRepository repository;
    private final CardMapper mapper;

    @Override
    public Card save(Card card) {
        var document = mapper.toDocument(card);
        var savedDocument = repository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public List<Card> findAllByAccountId(String accountId) {
        return repository.findAllByAccountId(accountId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Card> findByIdAndAccountId(String cardId, String accountId) {
        return repository.findByIdAndAccountId(cardId, accountId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(String cardId) {
        repository.deleteById(cardId);
    }
}
