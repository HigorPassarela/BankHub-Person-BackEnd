package com.bankhub.hubia.application.graph.node;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityCheckNode {

    private final StringRedisTemplate redisTemplate;
    private static  final String REDIS_KEY_PREFIX = "status:account:";

    /**
     * Executa a verificação de segurança no cache.
     *
     * @param accountId ID da conta (extraído do Token JWT).
     * @throws SecurityException se a conta não estiver ativa.
     */
    public void execute(String accountId) {
        String redisKey = REDIS_KEY_PREFIX + accountId;
        log.debug("SecurityCheckNode: Verificando Guardrail para a conta {} no Redis.", accountId);

        String status = redisTemplate.opsForValue().get(redisKey);

        if (!"ACTIVE".equals(status)) {
            log.warn("SecurityCheckNode: Acesso negado pela IA. Status da conta {} é [{}]", accountId, status);
            throw new SecurityException("Acesso negado. A sua conta está inativa ou bloqueada, portanto o Assistente de IA não pode prosseguir com o atendimento.");
        }

        log.info("SecurityCheckNode: Verificação concluída. Conta {} está ativa e liberada para uso da IA.", accountId);
    }
}
