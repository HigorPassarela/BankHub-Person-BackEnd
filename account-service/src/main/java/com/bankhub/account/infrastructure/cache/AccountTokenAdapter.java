package com.bankhub.account.infrastructure.cache;

import com.bankhub.account.application.port.out.AccountTokenPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTokenAdapter implements AccountTokenPort {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "activation:token:";
    private static final int EXPIRATION_HOURS = 24;

    @Override
    public String generateAndSaveToken(String accountId) {
        String rawToken = UUID.randomUUID().toString();
        String redisKey = PREFIX + rawToken;

        log.debug("Gerando Token Efêmero (Magic Link) de 24h para a conta {}", accountId);

        redisTemplate.opsForValue().set(redisKey, accountId, Duration.ofHours(EXPIRATION_HOURS));

        return rawToken;
    }

    @Override
    public Optional<String> resolveToken(String token) {
        String redisKey = PREFIX + token;
        String accountId = redisTemplate.opsForValue().get(redisKey);

        return Optional.ofNullable(accountId);
    }

    @Override
    public void revokeToken(String token) {
        String redisKey = PREFIX + token;
        log.debug("Revogando Magic Link. Queimando chave no Redis: {}", redisKey);
        redisTemplate.delete(redisKey);
    }
}