package com.bankhub.hubia.application.graph.node;

import com.bankhub.hubia.infrastructure.client.AccountClientResponse;
import com.bankhub.hubia.infrastructure.client.AccountFeignClient;
import dev.langchain4j.agent.tool.Tool;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTools {

    private final AccountFeignClient accountFeignClient;

    /**
     * Ferramenta disponibilizada para o LLM. A descrição na anotação @Tool
     * atua como prompt dinâmico para o raciocínio da IA.
     */
    @Tool("Busca os dados completos da conta bancária de um cliente, incluindo o saldo atual e o status da conta.")
    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackGetAccountData")
    public String getAccountData(String accountId, String customerId) {
        log.info("Tool Executor: IA solicitou os dados da conta {} para o cliente {}", accountId, customerId);

        AccountClientResponse response = accountFeignClient.getAccount(accountId, customerId);

        return String.format(
                "A conta [%s] encontra-se no status [%s]. O saldo atual é de [%s %s]. A última atualização ocorreu em [%s].",
                response.account(),
                response.status(),
                response.balance().valor(),
                response.balance().moeda(),
                response.lastUpdate()
        );
    }

    /**
     * Fallback do Circuit Breaker. Se o account-service estiver fora do ar (timeout/error),
     * a IA receberá essa resposta e adaptará a sua fala para o usuário.
     */
    public String fallbackGetAccountData(String accountId, String customerId, Throwable ex) {
        log.error("Circuit Breaker Aberto: Falha ao comunicar com account-service. Motivo: {}", ex.getMessage());
        return "INFORMAÇÃO INDISPONÍVEL: O sistema central de contas está temporariamente inoperante devido a uma manutenção. Peça ao usuário para tentar novamente em alguns minutos.";
    }
}
