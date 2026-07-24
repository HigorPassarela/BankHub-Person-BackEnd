package com.bankhub.investment.infrastructure.adapter.out.persistence;

import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.mapper.PortfolioMapper;
import com.bankhub.investment.infrastructure.persistence.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PortfolioPersistenceAdapter implements PortfolioPersistencePort {

    private final PortfolioRepository repository;
    private final PortfolioMapper mapper;

    @Override
    public Portfolio save(Portfolio portfolio) {
        var document = mapper.toDocument(portfolio);
        var savedDocument = repository.save(document);
        return mapper.toDomain(savedDocument);
    }

    @Override
    public Optional<Portfolio> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId)
                .map(mapper::toDomain);
    }
}
