package com.bankhub.investment.application.port.out.client;

import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.infrastructure.client.AccountFeignClient;
import com.bankhub.investment.infrastructure.client.dto.DebitRequest;
import com.bankhub.investment.infrastructure.messaging.dto.RefundCommandMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDebitAdapter implements AccountDebitPort {

    private final AccountFeignClient feignClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String COMMAND_TOPIC = "bankhub.account.commands";

    @Override
    public void debitFunds(String accountId, String customerId, BigDecimal amount) {
        log.info("Adapter: Solicitando débito de R$ {} na conta {} para compra de ativos.", amount, accountId);

        try {
            DebitRequest payload = new DebitRequest(amount);

            feignClient.debitAccount(accountId, customerId, payload);

            log.info("Débito autorizado com sucesso pelo Account Service.");
        } catch (Exception e) {
            log.error("Transação Recusada: Falha ao debitar a conta {}. Motivo da rede: {}", accountId, e.getMessage());
            throw new IllegalStateException("O banco recusou o débito. Verifique seu saldo ou o status da sua conta.");
        }
    }

    @Override
    public void refundFunds(String accountId, String customerId, BigDecimal amount) {
        log.warn("Saga Compensation: Solicitando estorno de R$ {} para a conta {}.", amount, accountId);

        try {
            RefundCommandMessage command = RefundCommandMessage.builder()
                    .header(RefundCommandMessage.Header.builder()
                            .correlationId(UUID.randomUUID().toString())
                            .eventType("REFUND_ACCOUNT_COMMAND")
                            .timestamp(LocalDateTime.now())
                            .build())
                    .payload(RefundCommandMessage.Payload.builder()
                            .accountId(accountId)
                            .customerId(customerId)
                            .amount(amount)
                            .build())
                    .build();

            kafkaTemplate.send(COMMAND_TOPIC, accountId, command);

            log.info("Saga Compensation: Comando de estorno publicado com sucesso no Kafka.");
        } catch (Exception e) {
            log.error("FALHA CRÍTICA NO MENSAGEIRO! Intervenção manual requerida para estorno. Conta: {}, Erro: {}", accountId, e.getMessage());
        }
    }
}
