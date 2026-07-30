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
    @Tool("Busca os dados completos da conta bancária de um cliente, incluindo agência, número, saldo e status.")
    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackGetAccountData")
    public String getAccountData(String accountId, String customerId) {
        log.info("Tool Executor: IA solicitou os dados da conta {} para o cliente {}", accountId, customerId);

        AccountClientResponse response = accountFeignClient.getAccount(accountId, customerId);

        return String.format(
                "A conta é da Agência [%s] e Número [%s]. O status é [%s]. O saldo atual é de [%s %s]. A última atualização ocorreu em [%s].",
                response.bankDetails().agency(),
                response.bankDetails().number(),
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

    /**
     * Ferramenta disponibilizada para o LLM. A string no @Tool ensina a IA quando e como usar.
     */
    @Tool("Atualiza o perfil de investidor (Suitability) do cliente no banco de dados. " +
            "Valores permitidos para 'profile': CONSERVATIVE, MODERATE ou AGGRESSIVE. " +
            "Use esta ferramenta APENAS após conversar com o cliente e identificar o seu apetite a risco.")
    @CircuitBreaker(name = "accountServiceCB", fallbackMethod = "fallbackUpdateProfile")
    public String updateInvestorProfileTool(String accountId, String customerId, String profile) {
        log.info("Tool Executor: IA solicitou a atualização do perfil da conta {} para [{}]", accountId, profile);

        AccountClientResponse response = accountFeignClient.updateInvestorProfile(accountId, customerId, profile);

        return String.format(
                "SUCESSO: O perfil da conta foi atualizado para [%s]. Agora responda ao cliente confirmando a atualização e ofereça ativos de investimento compatíveis com este perfil.",
                response.investorProfile() != null ? response.investorProfile() : profile
        );
    }

    /**
     * Fallback do Circuit Breaker. Se o account-service estiver fora do ar,
     * a IA será avisada e poderá dar uma resposta amigável em vez de travar.
     */
    public String fallbackUpdateProfile(String accountId, String customerId, String profile, Throwable ex) {
        log.error("Circuit Breaker Aberto: Falha ao salvar Suitability no account-service. Motivo: {}", ex.getMessage());
        return "FALHA DO SISTEMA INTERNO: O serviço de cadastro está inoperante. Informe ao cliente, de forma muito educada, que você analisou o perfil dele como " + profile + ", mas que devido a uma instabilidade momentânea, o perfil não pôde ser salvo agora. Peça para tentar novamente mais tarde.";
    }
}
