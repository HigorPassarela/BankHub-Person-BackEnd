package com.bankhub.gateway.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class IdempotencyGatewayFilterFactory extends AbstractGatewayFilterFactory<IdempotencyGatewayFilterFactory.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";

    public IdempotencyGatewayFilterFactory(ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String idempotencyKey = exchange.getRequest().getHeaders().getFirst("Idempotency-Key");

            if (idempotencyKey == null || idempotencyKey.isBlank()) {
                log.warn("Tentativa de chamada crítica sem Idempotency-Key. Bloqueando por segurança.");
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;

            return redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofSeconds(10))
                    .flatMap(isNewRequest -> {
                        if (Boolean.TRUE.equals(isNewRequest)) {
                            log.debug("Idempotency-Key [{}] aceita. Repassando requisição.", idempotencyKey);
                            return chain.filter(exchange);
                        } else {
                            log.warn("Dedo Nervoso Detectado! Requisição duplicada bloqueada. Idempotency-Key: {}", idempotencyKey);

                            exchange.getResponse().setStatusCode(HttpStatus.CONFLICT);
                            return exchange.getResponse().setComplete();
                        }
                    });
        };
    }

    public static class Config {
    }
}