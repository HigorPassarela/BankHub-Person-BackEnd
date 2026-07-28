package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ActivateAccountUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.AccountTokenPort; // A NOVA PORTA QUE VAMOS CRIAR!
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
    private final AccountTokenPort tokenPort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Account execute(String activationToken) {
        log.info("Iniciando ativação de conta. Resolvendo Magic Link Token...");

        String accountId = tokenPort.resolveToken(activationToken)
                .orElseThrow(() -> new AccountNotFoundException("Token inválido ou expirado. O link pode já ter sido utilizado ou passou do prazo de 24 horas."));

        log.info("Token válido! O dono do token é a conta ID: {}", accountId);

        // 2. Busca a conta no Banco de Dados
        Account account = persistencePort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta vinculada ao token não encontrada no sistema."));

        if (account.status() != AccountStatus.PENDING_ACTIVATION) {
            log.warn("Tentativa de ativação inválida. A conta {} está com status {}", accountId, account.status());
            throw new IllegalStateException("Esta conta já foi ativada ou encontra-se bloqueada.");
        }

        Account activatedAccount = account.activate();

        Account savedAccount = persistencePort.save(activatedAccount);

        log.info("Conta {} ativada com sucesso!", savedAccount.id());

        tokenPort.revokeToken(activationToken);

        eventPublisher.publishEvent(new AccountStatusChangedEvent(savedAccount));

        return savedAccount;
    }
}