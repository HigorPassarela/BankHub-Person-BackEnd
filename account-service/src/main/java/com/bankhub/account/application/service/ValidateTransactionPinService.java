package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ValidateTransactionPinUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateTransactionPinService implements ValidateTransactionPinUseCase {

    private final AccountPersistencePort persistencePort;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String PIN_ATTEMPTS_PREFIX = "security:pin:attempts:";
    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_DURATION_MINUTES = 15;

    @Override
    public boolean execute(String accountId, String customerId, String plainPin) {
        log.info("Iniciando validação de PIN transacional para a conta: {}. Solicitante: {}", accountId, customerId);

        // 1. Verifica se a conta já está bloqueada no Redis
        String redisKey = PIN_ATTEMPTS_PREFIX + accountId;
        String attemptsStr = redisTemplate.opsForValue().get(redisKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Alerta de Segurança: Tentativa de PIN bloqueada por Brute-Force. Conta: {}", accountId);
            throw new SecurityException("Conta temporariamente bloqueada para transações devido a múltiplas tentativas incorretas de PIN. Tente novamente em " + BLOCK_DURATION_MINUTES + " minutos.");
        }

        // 2. Busca a conta no banco
        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        if (account.transactionPinHash() == null || account.transactionPinHash().isBlank()) {
            log.error("Tentativa de transação bloqueada. Conta {} não possui PIN cadastrado.", accountId);
            throw new SecurityException("Transação negada. Nenhum PIN de segurança cadastrado para esta conta.");
        }

        // 3. Valida o hash
        boolean isMatch = passwordEncoder.matches(plainPin, account.transactionPinHash());

        if (!isMatch) {
            // Incrementa o contador de falhas e renova o tempo de bloqueio
            long newAttempts = redisTemplate.opsForValue().increment(redisKey);
            redisTemplate.expire(redisKey, Duration.ofMinutes(BLOCK_DURATION_MINUTES));
            
            log.warn("Alerta de Segurança: Tentativa de transação com PIN INVÁLIDO. Conta: {}. Tentativas: {}/{}", accountId, newAttempts, MAX_ATTEMPTS);
            throw new SecurityException("A assinatura eletrônica (PIN) fornecida está incorreta. Tentativa " + newAttempts + " de " + MAX_ATTEMPTS + ".");
        }

        // 4. Sucesso: reseta o contador de tentativas
        redisTemplate.delete(redisKey);

        log.info("Validação de segurança concluída com sucesso. PIN aprovado para a conta: {}", accountId);
        return true;
    }
}
