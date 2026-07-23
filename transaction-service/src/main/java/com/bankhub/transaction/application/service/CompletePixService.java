package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.CompletePixUseCase;
import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletePixService implements CompletePixUseCase {

    private final TransactionPersistencePort persistencePort;

    @Override
    @Transactional
    public void execute(String transactionId, String sagaStatus, String failureReason) {
        log.info("Fechando Ledger para a transação {}. Status da Saga: {}", transactionId, sagaStatus);

        Transaction transaction = persistencePort.findById(transactionId)
                .orElseThrow(() -> new IllegalStateException("Transação não encontrada para fechamento da Saga."));

        Transaction finalTransaction;

        if ("COMPLETED".equals(sagaStatus)) {
            finalTransaction = transaction.complete();
        } else {
            String reason = "OK".equals(failureReason) ? "Falha interna no motor de contas" : failureReason;
            finalTransaction = transaction.fail(reason);
        }

        persistencePort.save(finalTransaction);

        log.info("PIX {} liquidado no Ledger com status definitivo: {}", finalTransaction.id(), finalTransaction.status());
    }
}
