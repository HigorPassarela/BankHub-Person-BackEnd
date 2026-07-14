package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.FindAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindAccountService implements FindAccountUseCase {

    private final AccountPersistencePort persistencePort;

    @Override
    public Account execute(String accountId, String customerId) {
        log.info("Iniciando busca da conta: {} para o cliente: {}", accountId, customerId);

        return persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> {
                    log.warn("Falha de segurança/negócio: conta {} não encontrada ou cliente {} não é titular", accountId, customerId);
                    return new AccountNotFoundException("Conta não encontrada ou acesso negado para este cliente.");
                });
    }
}
