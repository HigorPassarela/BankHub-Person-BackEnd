package com.bankhub.investment.application.service;

import com.bankhub.investment.application.port.in.GetPortfolioUseCase;
import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Portfolio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPortfolioService implements GetPortfolioUseCase {

    private final PortfolioPersistencePort persistencePort;

    @Override
    public Portfolio execute(String customerId) {
        log.info("Buscando portfólio de investimentos para o cliente: {}", customerId);

        return persistencePort.findByCustomerId(customerId)
                .orElseGet(() -> Portfolio.builder()
                        .customerId(customerId)
                        .assets(Collections.emptyList())
                        .build());
    }
}
