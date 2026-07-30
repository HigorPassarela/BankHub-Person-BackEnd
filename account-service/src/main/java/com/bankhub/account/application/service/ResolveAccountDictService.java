package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ResolveAccountDictUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolveAccountDictService implements ResolveAccountDictUseCase {

    private final AccountPersistencePort persistencePort;

    @Override
    public Account execute(String accountNumber) {
        log.info("Resolvendo chave PIX (Número da Conta): {}", accountNumber);

        Account account = persistencePort.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Chave PIX inválida. Conta não encontrada no diretório."));

        if (account.status() != AccountStatus.ACTIVE) {
            log.warn("Chave PIX atrelada a uma conta inativa. Abortando resolução. Conta ID: {}", account.id());
            throw new IllegalStateException("Esta conta não está apta a receber transferências no momento.");
        }

        return account;
    }
}