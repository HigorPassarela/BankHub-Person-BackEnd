package com.bankhub.investment.application.port.out;

import com.bankhub.investment.domain.Portfolio;
import java.util.Optional;

public interface PortfolioPersistencePort {
    Portfolio save(Portfolio portfolio);
    Optional<Portfolio> findByCustomerId(String customerId);
}