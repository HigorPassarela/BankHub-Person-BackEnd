package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Account execute(String customerId) {
        log.info("Iniciando a criação de conta para o cliente: {}", customerId);

        AccountNumber generatedNumber = generateBankCoordinates();

        Account newAccount = Account.builder()
                .customerId(customerId)
                .accountNumber(generatedNumber)
                .balance(Balance.zero())
                .status(AccountStatus.PENDING_ACTIVATION)
                .build();

        Account savedAccount = persistencePort.save(newAccount);

        log.info("Conta criada no banco aguardando ativação. ID Interno: {}, Ag/Conta: {}, Status: {}",
                savedAccount.id(), savedAccount.accountNumber().getFormatted(), savedAccount.status());

        eventPublisher.publishEvent(new AccountCreatedEvent(savedAccount));

        return savedAccount;
    }

    /**
     * Gera um número de conta e agência simulando uma emissão bancária real.
     */
    private AccountNumber generateBankCoordinates() {
        String defaultAgency = "0001";

        String uniqueHash = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        int digit = new Random().nextInt(10);

        String formattedAccount = String.format("%s-%d", uniqueHash, digit);

        return new AccountNumber(defaultAgency, formattedAccount);
    }
}
