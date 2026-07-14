package com.bankhub.account.infrastructure.cache;

import com.bankhub.account.domain.event.AccountCreatedEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRedisSyncListener {

    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "status:account";

    /**
     * Ouve o evento de criação da conta após o sucesso no banco de dados.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retry(name = "redisRetry", fallbackMethod = "fallbackRedisSync")
    public void syncAccountStatusToRedis(AccountCreatedEvent event) {
        String accountId = event.account().id();
        String status = event.account().status().name();
        String redisKey = REDIS_KEY_PREFIX + accountId;

        log.info("Sincronizando status da conta no Redis. Chave: {}, Status: {}", redisKey, status);

        redisTemplate.opsForValue().set(redisKey, status);

        log.info("Status da conta {} gravado no Redis com sucesso.", accountId);
    }

    /**
     * Fallback do Resilience4j caso o cluster Redis esteja inoperante.
     */
    public void fallbackRedisSync(AccountCreatedEvent event, Exception ex) {
        log.error("CRÍTICO: Falha ao sincronizar o status da conta {} no Redis. O API Gateway poderá bloquear requisições. Motivo: {}",
                event.account().id(), ex.getMessage());
        // Aqui um alerta de monitoramento (ex: Datadog/Prometheus) seria disparado.
    }
}
