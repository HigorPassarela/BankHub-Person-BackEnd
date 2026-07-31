package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.InitiatePixUseCase;
import com.bankhub.transaction.application.port.out.TransactionEventPublisherPort;
import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import com.bankhub.transaction.infrastructure.client.AccountFeignClient;
import com.bankhub.transaction.infrastructure.client.dto.PinValidationRequest;
import feign.FeignException;
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
    private final AccountFeignClient accountFeignClient;

    @Override
    @Transactional
    public Transaction execute(String customerId, String sourceAccountId, String destinationAccountId, BigDecimal amount, String transactionPin, String category) {
        log.info("Iniciando Transação PIX. Solicitante: {}, Origem: {}, Destino: {}, Valor: {}",
                customerId, sourceAccountId, destinationAccountId, amount);

        if (sourceAccountId.equals(destinationAccountId)) {
            log.warn("Fraude/Erro detectado: Tentativa de PIX para a própria conta. Conta: {}", sourceAccountId);
            throw new IllegalArgumentException("Não é possível realizar uma transferência para a própria conta.");
        }

        try {
            log.info("Acionando Account Service para validação de KYC e Senha Transacional...");

            accountFeignClient.validateTransaction(sourceAccountId, customerId, new PinValidationRequest(transactionPin));
            log.info("Validação de segurança aprovada! Autorizando PIX.");
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            log.warn("Falha de Segurança: O Account Service recusou a transação. Motivo do Feign: {}", e.getMessage());
            throw new SecurityException("Transação negada: A sua senha está incorreta ou o KYC (Selfie) está pendente.");
        } catch (Exception e) {
            log.error("Erro na comunicação M2M com Account Service: {}", e.getMessage());
            throw new IllegalStateException("O serviço de validação do banco está indisponível. Tente novamente em instantes.");
        }

        TransactionCategory resolvedCategory = TransactionCategory.OTHER;
        if (category != null && !category.isBlank()) {
            try {
                resolvedCategory = TransactionCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Categoria enviada pelo Front [{}] é inválida. Assumindo OTHER.", category);
            }
        }

        Transaction newTransaction = Transaction.builder()
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(resolvedCategory)
                .build();

        Transaction savedTransaction = persistencePort.save(newTransaction);

        log.info("Transação registrada no Ledger com sucesso. Status PENDING. ID: {}", savedTransaction.id());

        eventPublisherPort.publishTransactionInitiatedEvent(savedTransaction);

        return savedTransaction;
    }
}
