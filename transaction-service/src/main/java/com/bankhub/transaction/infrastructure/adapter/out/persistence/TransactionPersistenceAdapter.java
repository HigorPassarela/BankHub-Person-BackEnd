package com.bankhub.transaction.infrastructure.adapter.out.persistence;

import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.mapper.TransactionMapper;
import com.bankhub.transaction.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        var document = mapper.toDocument(transaction);
        var savedDocument = repository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Transaction> findById(String transactionId) {
        return repository.findById(transactionId)
                .map(mapper::toDomain);
    }
}
