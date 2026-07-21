package com.bankhub.account.infrastructure.cache;

import com.bankhub.account.domain.event.AccountCreatedEvent;
import com.bankhub.account.domain.event.AccountStatusChangedEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRedisSyncListener {

    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "status:account:";

    /**
     * Ouve o evento de NASCIMENTO da conta (Gravará PENDING_ACTIVATION no Redis).
     */
    @EventListener
    @Retry(name = "redisRetry", fallbackMethod = "fallbackRedisSync")
    public void handleAccountCreatedEvent(AccountCreatedEvent event) {
        String accountId = event.account().id();
        String status = event.account().status().name();

        saveToRedis(accountId, status);
    }

    /**
     * NOVO: Ouve o evento de MUDANÇA DE STATUS da conta (Sobrescreverá para ACTIVE no Redis).
     */
    @EventListener
    @Retry(name = "redisRetry", fallbackMethod = "fallbackRedisSync")
    public void handleAccountStatusChangedEvent(AccountStatusChangedEvent event) {
        String accountId = event.account().id();
        String status = event.account().status().name();

        log.info("Evento de mudança de status capturado. Atualizando Redis para a conta: {}", accountId);
        saveToRedis(accountId, status);
    }

    /**
     * Metodo interno reutilizável para a gravação física no cache.
     */
    private void saveToRedis(String accountId, String status) {
        String redisKey = REDIS_KEY_PREFIX + accountId;
        log.info("Sincronizando status da conta no Redis. Chave: {}, Status: {}", redisKey, status);

        redisTemplate.opsForValue().set(redisKey, status);
        log.info("Status da conta gravado no Redis com sucesso.");
    }

    /**
     * Fallback do Resilience4j caso o cluster Redis esteja inoperante.
     */
    public void fallbackRedisSync(Object event, Exception ex) {
        log.error("CRÍTICO: Falha ao sincronizar o status da conta no Redis. Motivo: {}", ex.getMessage());
    }
}