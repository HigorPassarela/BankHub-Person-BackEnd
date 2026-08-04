package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.AccountTokenPort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountNumber;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.event.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

    private final AccountPersistencePort persistencePort;
    private final AccountTokenPort tokenPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Account execute(String customerId, String fullName, String phone, String address) {
        log.info("Iniciando a criação de conta para: {}", fullName);

        AccountNumber generatedNumber = generateBankCoordinates();

        Account newAccount = Account.builder()
                .customerId(customerId)
                .fullName(fullName)
                .phone(phone)
                .address(address)
                .accountNumber(generatedNumber)
                .balance(Balance.zero())
                .status(AccountStatus.PENDING_ACTIVATION)
                .build();

        Account savedAccount = persistencePort.save(newAccount);

        log.info("Conta criada no banco. ID Interno: {}, Ag/Conta: {}", savedAccount.id(), savedAccount.accountNumber().getFormatted());

        String activationToken = tokenPort.generateAndSaveToken(savedAccount.id());
        eventPublisher.publishEvent(new AccountCreatedEvent(savedAccount, activationToken));

        return savedAccount;
    }

    private AccountNumber generateBankCoordinates() {
        String defaultAgency = "0001";
        String uniqueHash = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        int digit = new Random().nextInt(10);
        return new AccountNumber(defaultAgency, String.format("%s-%d", uniqueHash, digit));
    }
}