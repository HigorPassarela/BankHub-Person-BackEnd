package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.GetStatementUseCase;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.infrastructure.mapper.TransactionMapper;
import com.bankhub.transaction.infrastructure.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStatementService implements GetStatementUseCase {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;

    @Override
    public List<Transaction> execute(String accountId) {
        log.info("Buscando extrato financeiro para a conta: {}", accountId);

        return repository.fetchStatementByAccountId(accountId, accountId)
                .stream()
                .map(mapper::toDomain)
                .sorted((t1, t2) -> t2.createdAt().compareTo(t1.createdAt()))
                .toList();
    }
}
