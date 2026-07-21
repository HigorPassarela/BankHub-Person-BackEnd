package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ActivateAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.event.AccountStatusChangedEvent;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivateAccountService implements ActivateAccountUseCase {

    private final AccountPersistencePort persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Account execute(String accountId, String customerId) {
        log.info("Iniciando ativação da conta {} para o cliente {}", accountId, customerId);

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        if (account.status() != AccountStatus.PENDING_ACTIVATION) {
            log.warn("Tentativa de ativação inválida. A conta {} está com status {}", accountId, account.status());
            throw new IllegalStateException("Esta conta não está pendente de ativação.");
        }

        Account activatedAccount = account.activate();

        Account savedAccount = persistencePort.save(activatedAccount);

        log.info("Conta {} ativada com sucesso!", savedAccount.id());

        eventPublisher.publishEvent(new AccountStatusChangedEvent(savedAccount));

        return savedAccount;
    }
}
