package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.DebitAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebitAccountService implements DebitAccountUseCase {

    private final AccountPersistencePort persistencePort;

    @Override
    @Transactional
    public Account execute(String accountId, String customerId, BigDecimal amount) {
        log.info("Iniciando Débito M2M de R$ {} para a conta {}. Solicitante: {}", amount, accountId, customerId);

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        if (account.status() != AccountStatus.ACTIVE) {
            log.warn("Débito recusado. A conta {} está no status {}", accountId, account.status());
            throw new IllegalStateException("Débitos só são permitidos em contas ativas.");
        }

        Account richAccount = account.debit(amount);

        Account savedAccount = persistencePort.save(richAccount);

        log.info("Débito efetuado com sucesso! Novo saldo: R$ {}", savedAccount.balance().amount());

        return savedAccount;
    }
}
