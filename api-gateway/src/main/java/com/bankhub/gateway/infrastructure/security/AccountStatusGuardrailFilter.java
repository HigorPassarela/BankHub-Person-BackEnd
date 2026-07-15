package com.bankhub.gateway.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AccountStatusGuardrailFilter extends AbstractGatewayFilterFactory<AccountStatusGuardrailFilter.Config> {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final String REDIS_KEY_PREFIX = "status:account:";

    public AccountStatusGuardrailFilter(ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Map<String, String> uriVariables = exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            if (uriVariables == null || !uriVariables.containsKey("accountId")) {
                return chain.filter(exchange);
            }

            String accountId = uriVariables.get("accountId");
            String redisKey = REDIS_KEY_PREFIX + accountId;

            log.debug("Guardrail: Verificando status da conta {} no Redis", accountId);

            return redisTemplate.opsForValue().get(redisKey)
                    .defaultIfEmpty("UNKNOWN")
                    .flatMap(status -> {
                        if ("ACTIVE".equals(status)) {
                            return chain.filter(exchange);
                        } else {
                            log.warn("Guardrail de Segurança: Acesso negado para a conta {}. Status no Redis: {}", accountId, status);
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }
                    });
        };
    }

    /**
     * Classe de configuração estática requerida pelo padrão de Filter Factory do Spring Cloud Gateway.
     */
    public static class Config {
        // Pode ser estendida futuramente para aceitar parâmetros customizados via application.yml
    }
}
