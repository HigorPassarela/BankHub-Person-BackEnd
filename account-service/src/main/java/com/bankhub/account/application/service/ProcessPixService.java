package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ProcessPixUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.event.PixProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPixService implements ProcessPixUseCase {

    private final AccountPersistencePort persistencePort;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(String transactionId, String sourceAccountId, String destinationAccountId, BigDecimal amount) {
        log.info("Processando transação PIX {}. Valor: {} | Origem (Conta): {} -> Destino (Conta): {}",
                transactionId, amount, sourceAccountId, destinationAccountId);

        try {
            Account sourceAccount = persistencePort.findById(sourceAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada ou inválida."));

            Account destinationAccount = persistencePort.findById(destinationAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada."));

            if (sourceAccount.status() != AccountStatus.ACTIVE) {
                throw new IllegalStateException("A conta de origem está inativa ou bloqueada.");
            }
            if (destinationAccount.status() != AccountStatus.ACTIVE) {
                throw new IllegalStateException("A conta de destino não pode receber transferências no momento.");
            }

            Account debitedAccount = sourceAccount.debit(amount);
            Account creditedAccount = destinationAccount.credit(amount);

            persistencePort.save(debitedAccount);

            try {
                persistencePort.save(creditedAccount);
            } catch (Exception ex) {
                log.error("Erro crítico ao salvar o crédito no destino. Executando Rollback Manual na conta de origem...");

                Account rollbackAccount = debitedAccount.credit(amount);
                persistencePort.save(rollbackAccount);

                throw new IllegalStateException("Falha inesperada ao salvar o crédito no destino. O valor foi estornado.", ex);
            }

            log.info("PIX {} processado com sucesso! Contas atualizadas no MongoDB.", transactionId);
            eventPublisher.publishEvent(new PixProcessedEvent(transactionId, "COMPLETED", null));

        } catch (Exception e) {
            log.error("FALHA na transação PIX {}. Motivo: {}", transactionId, e.getMessage());
            eventPublisher.publishEvent(new PixProcessedEvent(transactionId, "FAILED", e.getMessage()));
        }
    }
}