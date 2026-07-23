package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.InitiatePixUseCase;
import com.bankhub.transaction.application.port.out.TransactionEventPublisherPort;
import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitiatePixService implements InitiatePixUseCase {

    private final TransactionPersistencePort persistencePort;
    private final TransactionEventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public Transaction execute(String sourceAccountId, String destinationAccountId, BigDecimal amount) {
        log.info("Iniciando Transação PIX. Origem: {}, Destino: {}, Valor: {}",
                sourceAccountId, destinationAccountId, amount);

        Transaction newTransaction = Transaction.builder()
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = persistencePort.save(newTransaction);

        log.info("Transação registrada no Ledger com sucesso. Status PENDING. ID: {}", savedTransaction.id());

        eventPublisherPort.publishTransactionInitiatedEvent(savedTransaction);

        return savedTransaction;
    }
}
