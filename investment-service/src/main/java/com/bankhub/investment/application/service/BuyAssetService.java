package com.bankhub.investment.application.service;

import com.bankhub.investment.application.port.in.BuyAssetUseCase;
import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Asset;
import com.bankhub.investment.domain.AssetType;
import com.bankhub.investment.domain.Portfolio;
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

    @Override
    @Transactional
    public Portfolio execute(String customerId, String accountId, String ticker, String type, BigDecimal quantity, String jwtToken) {
        log.info("Iniciando Ordem de Compra. Ticker: {}, Cotas: {}. Cliente: {}", ticker, quantity, customerId);

        // 1. Simula a consulta do Preço atual no Mercado (Ex: B3, Nasdaq)
        BigDecimal currentMarketPrice = fetchMarketPrice(ticker);
        log.debug("Cotação atual do ativo {}: R$ {}", ticker, currentMarketPrice);

        // 2. Calcula o custo total da operação
        BigDecimal totalCost = currentMarketPrice.multiply(quantity);
        log.info("Custo total da operação: R$ {}", totalCost);

        // 3. Comunicação M2M: Tenta debitar da conta corrente (Se falhar, a operação é abortada aqui!)
        accountDebitPort.debitFunds(accountId, customerId, jwtToken, totalCost);

        // 4. Cria o objeto do novo ativo
        Asset purchasedAsset = new Asset(ticker, AssetType.valueOf(type.toUpperCase()), quantity, currentMarketPrice);

        // 5. Busca a carteira do cliente ou cria uma virgem se for o primeiro investimento da vida dele
        Portfolio portfolio = persistencePort.findByCustomerId(customerId)
                .orElseGet(() -> Portfolio.builder().customerId(customerId).build());

        // 6. Adiciona o ativo (Calculando preço médio e agrupando tickers repetidos magicamente no Domínio)
        Portfolio updatedPortfolio = portfolio.addAsset(purchasedAsset);

        // 7. Salva a nova carteira no MongoDB (Protegida por Optimistic Locking @Version)
        Portfolio savedPortfolio = persistencePort.save(updatedPortfolio);

        log.info("Ordem executada com sucesso! O Ativo {} foi adicionado à carteira do cliente.", ticker);

        return savedPortfolio;
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
