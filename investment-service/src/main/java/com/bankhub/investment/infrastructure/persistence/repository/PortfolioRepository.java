package com.bankhub.investment.infrastructure.persistence.repository;

import com.bankhub.investment.infrastructure.persistence.entity.PortfolioDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends MongoRepository<PortfolioDocument, String> {

    /**
     * Busca a carteira consolidada de um cliente específico.
     * PERFORMANCE: Uso explícito do BSON Query para evitar overhead de tradução do Spring.
     */
    @Query("{ 'customerId' : ?0 }")
    Optional<PortfolioDocument> findByCustomerId(String customerId);
}
