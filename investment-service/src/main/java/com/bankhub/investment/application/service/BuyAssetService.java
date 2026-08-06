package com.bankhub.investment.application.service;

import com.bankhub.investment.application.port.in.BuyAssetUseCase;
import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Asset;
import com.bankhub.investment.domain.AssetType;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.client.AccountFeignClient;
import com.bankhub.investment.infrastructure.client.TransactionFeignClient;
import com.bankhub.investment.infrastructure.client.dto.PinValidationRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyAssetService implements BuyAssetUseCase {

    private final PortfolioPersistencePort persistencePort;
    private final AccountDebitPort accountDebitPort;
    private final AccountFeignClient accountFeignClient;
    private final TransactionFeignClient transactionFeignClient;

    @Override
    @Transactional
    public Portfolio execute(String customerId, String accountId, String ticker, String type, BigDecimal quantity, String transactionPin) {
        
        try {
            accountFeignClient.validateTransaction(accountId, customerId, new PinValidationRequest(transactionPin));
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            String msg = "Transação negada.";
            try {
                String body = e.contentUTF8();
                if (body != null && body.contains("\"detail\":\"")) {
                    msg = body.split("\"detail\":\"")[1].split("\"")[0];
                }
            } catch (Exception ignored) {}
            throw new SecurityException(msg);
        }

        BigDecimal currentMarketPrice = ticker.startsWith("CDB") ? new BigDecimal("1000.00") : new BigDecimal("35.50");
        BigDecimal totalCost = currentMarketPrice.multiply(quantity);

        accountDebitPort.debitFunds(accountId, customerId, totalCost);

        try {
            Asset purchasedAsset = new Asset(ticker, AssetType.valueOf(type.toUpperCase()), quantity, currentMarketPrice);
            Portfolio portfolio = persistencePort.findByCustomerId(customerId).orElseGet(() -> Portfolio.builder().customerId(customerId).build());
            Portfolio savedPortfolio = persistencePort.save(portfolio.addAsset(purchasedAsset));

            try {
                transactionFeignClient.registerLedger(Map.of("sourceAccountId", accountId, "destinationAccountId", "B3-EXCHANGE", "amount", totalCost, "category", "INVEST"));
            } catch (Exception ignored) {}

            return savedPortfolio;
        } catch (Exception e) {
            accountDebitPort.refundFunds(accountId, customerId, totalCost);
            throw new RuntimeException("Falha sistêmica ao registrar o ativo.", e);
        }
    }
}
