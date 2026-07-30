package com.bankhub.transaction.infrastructure.client;

import com.bankhub.transaction.infrastructure.client.dto.AccountDictResponse;
import com.bankhub.transaction.infrastructure.client.dto.PinValidationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente HTTP M2M para o serviço de contas (Resolvendo Chaves PIX no DICT Interno).
 */
@FeignClient(name = "account-service", url = "${bankhub.services.account.url:http://localhost:8081}")
public interface AccountFeignClient {

    /**
     * Busca os dados reais de uma conta para o PIX usando o número da conta como Chave.
     * Exige propagação do Token JWT.
     */
    @GetMapping("/api/v1/accounts/dict/{accountNumber}")
    AccountDictResponse resolveAccountByNumber(
            @PathVariable("accountNumber") String accountNumber
    );

    @PostMapping("/api/v1/accounts/{accountId}/validate-transaction")
    void validateTransaction(
            @PathVariable("accountId") String accountId,
            @RequestHeader("X-User-Id") String customerId,
            @RequestBody PinValidationRequest request
    );
}
