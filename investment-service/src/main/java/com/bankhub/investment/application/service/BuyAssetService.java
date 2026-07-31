package com.bankhub.investment.application.service;

import com.bankhub.investment.application.port.in.BuyAssetUseCase;
import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Asset;
import com.bankhub.investment.domain.AssetType;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.client.AccountFeignClient;
import com.bankhub.investment.infrastructure.client.dto.PinValidationRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BuyAssetService implements BuyAssetUseCase {

    private final PortfolioPersistencePort persistencePort;
    private final AccountDebitPort accountDebitPort;
    private final AccountFeignClient accountFeignClient;

    @Override
    @Transactional
    public Portfolio execute(String customerId, String accountId, String ticker, String type, BigDecimal quantity, String transactionPin) {
        log.info("Iniciando Ordem de Compra. Ticker: {}, Cotas: {}. Cliente: {}", ticker, quantity, customerId);

        try {
            log.info("Acionando Account Service para validação de KYC e Senha Transacional...");
            accountFeignClient.validateTransaction(accountId, customerId, new PinValidationRequest(transactionPin));
            log.info("Validação de segurança aprovada! Autorizando Investimento.");
        } catch (FeignException.Forbidden | FeignException.NotFound e) {
            log.warn("Falha de Segurança: O Account Service recusou a transação. Motivo do Feign: {}", e.getMessage());
            throw new SecurityException("Transação negada: A sua senha está incorreta ou o KYC (Selfie) está pendente.");
        } catch (Exception e) {
            log.error("Erro na comunicação M2M com Account Service: {}", e.getMessage());
            throw new IllegalStateException("O serviço de validação do banco está indisponível. Tente novamente em instantes.");
        }

        BigDecimal currentMarketPrice = fetchMarketPrice(ticker);
        BigDecimal totalCost = currentMarketPrice.multiply(quantity);

        accountDebitPort.debitFunds(accountId, customerId, totalCost);

        try {
            Asset purchasedAsset = new Asset(ticker, AssetType.valueOf(type.toUpperCase()), quantity, currentMarketPrice);

            Portfolio portfolio = persistencePort.findByCustomerId(customerId)
                    .orElseGet(() -> Portfolio.builder().customerId(customerId).build());

            Portfolio updatedPortfolio = portfolio.addAsset(purchasedAsset);

            Portfolio savedPortfolio = persistencePort.save(updatedPortfolio);

            log.info("Ordem executada com sucesso! O Ativo {} foi adicionado à carteira.", ticker);
            return savedPortfolio;

        } catch (Exception e) {
            log.error("Erro fatal ao salvar a carteira de ações! Disparando Rollback Compensatório M2M...");

            // SAGA COMPENSATION: Devolve o dinheiro para a conta do cliente!
            accountDebitPort.refundFunds(accountId, customerId, totalCost);

            // Repassa o erro pra frente para o Swagger avisar o cliente (e devolver o HTTP 500)
            throw new RuntimeException("Falha sistêmica ao registrar o ativo. O valor foi estornado para a sua conta.", e);
        }
    }

    /**
     * Mock de uma API Externa de Cotações (B3).
     */
    private BigDecimal fetchMarketPrice(String ticker) {
        // Em um ambiente real, chamaríamos um MarketDataFeignClient aqui.
        // Simulando que qualquer ação custa sempre R$ 35,50 e CDB custa R$ 1000,00.
        return ticker.startsWith("CDB") ? new BigDecimal("1000.00") : new BigDecimal("35.50");
    }
}
