package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountPersistencePort persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Account execute(String customerId) {
        log.info("Iniciando a criação de conta para o cliente: {}", customerId);

        Account newAccount = Account.builder()
                .customerId(customerId)
                .balance(Balance.zero())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = persistencePort.save(newAccount);

        log.info("Conta criada com sucesso no banco. ID: {}", savedAccount.id());

        return savedAccount;
    }
}
