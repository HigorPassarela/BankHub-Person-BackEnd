package com.bankhub.gateway.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtHeaderEnrichmentFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {

                    String userId = jwtAuth.getToken().getSubject();

                    log.debug("Security: Enriquecendo requisição com X-User-Id: {}", userId);

                    var mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Alta prioridade (-1) para garantir que os headers sejam injetados antes do roteamento
        return -1;
    }
}
