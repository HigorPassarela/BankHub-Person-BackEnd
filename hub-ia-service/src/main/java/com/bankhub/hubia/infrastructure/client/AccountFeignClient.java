package com.bankhub.hubia.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente HTTP declarativo para comunicação com o microsserviço account-service.
 */
@FeignClient(name = "account-service", url = "${bankhub.services.account.url}")
public interface AccountFeignClient {

    /**
     * Consulta os dados de uma conta repassando a identidade do usuário.
     *
     * @param accountId  ID da conta a ser consultada.
     * @param customerId ID do usuário logado (passado via Header de segurança).
     * @return DTO com os dados da conta (A ser implementado no próximo passo).
     */
    @GetMapping("/api/v1/accounts/{accountId}")
    AccountClientResponse getAccount(
            @PathVariable("accountId") String accountId,
            @RequestHeader("X-User-Id") String customerId
    );

    /**
     * Atualiza o perfil de investidor do cliente após análise do LLM.
     *
     * @param accountId  ID da conta.
     * @param customerId ID do usuário (Zero Trust).
     * @param profile    Perfil detectado pela IA (CONSERVATIVE, MODERATE, AGGRESSIVE).
     * @return O DTO atualizado da conta.
     */
    @PatchMapping("/api/v1/accounts/{accountId}/profile")
    AccountClientResponse updateInvestorProfile(
            @PathVariable("accountId") String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @RequestParam("profile") String profile
    );
}
