package com.bankhub.investment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "transaction-service", url = "http://bank-transaction-service:8085")
public interface TransactionFeignClient {

    @PostMapping("/api/v1/transactions/internal/ledger")
    void registerLedger(@RequestBody Map<String, Object> request);
}
