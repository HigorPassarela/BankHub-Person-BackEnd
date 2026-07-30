package com.bankhub.investment.infrastructure.client;

import com.bankhub.investment.infrastructure.client.dto.DebitRequest;
import com.bankhub.investment.infrastructure.client.dto.DepositRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente HTTP para integração M2M com o microsserviço de Contas bancárias.
 */
@FeignClient(name = "account-service", url = "${bankhub.services.account.url}")
public interface AccountFeignClient {

    /**
     * Solicita o débito financeiro na conta para a compra de ativos.
     * OBS: Este endpoint precisará ser criado no AccountController!
     */
    @PostMapping("/api/v1/accounts/{accountId}/debit")
    void debitAccount(
            @PathVariable("accountId") String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody DebitRequest request
    );

    @PostMapping("/api/v1/accounts/{accountId}/deposit")
    void refundAccount(
            @PathVariable("accountId") String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody DepositRequest request
    );
}
