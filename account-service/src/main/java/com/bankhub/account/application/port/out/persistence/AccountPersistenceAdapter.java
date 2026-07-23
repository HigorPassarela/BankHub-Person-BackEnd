package com.bankhub.account.application.port.out.persistence;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.infrastructure.mapper.AccountMapper;
import com.bankhub.account.infrastructure.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountPersistencePort {

    private final AccountRepository repository;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        var document = mapper.toDocument(account);
        var savedDocument = repository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Account> findByIdAndCustomerId(String id, String customerId) {
        return repository.findByIdAndCustomerId(id, customerId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Account> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Account> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }
}
