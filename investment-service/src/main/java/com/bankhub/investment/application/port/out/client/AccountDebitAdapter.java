package com.bankhub.investment.application.port.out.client;

import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.infrastructure.client.AccountFeignClient;
import com.bankhub.investment.infrastructure.client.dto.DebitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDebitAdapter implements AccountDebitPort {

    private final AccountFeignClient feignClient;

    @Override
    public void debitFunds(String accountId, String customerId, String jwtToken, BigDecimal amount) {
        log.info("Adapter: Solicitando débito de R$ {} na conta {} para compra de ativos.", amount, accountId);

        try {
            DebitRequest payload = new DebitRequest(amount);

            String bearerToken = "Bearer " + jwtToken;

            feignClient.debitAccount(accountId, customerId, bearerToken, payload);

            log.info("Débito autorizado com sucesso pelo Account Service.");
        } catch (Exception e) {
            log.error("Transação Recusada: Falha ao debitar a conta {}. Motivo da rede: {}", accountId, e.getMessage());
            throw new IllegalStateException("O banco recusou o débito. Verifique seu saldo ou o status da sua conta.");
        }
    }
}
