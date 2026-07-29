package com.bankhub.account.infrastructure.persistence.repository;

import com.bankhub.account.infrastructure.persistence.entity.CardDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends MongoRepository<CardDocument, String> {

    /**
     * Busca todos os cartões vinculados a uma conta específica.
     * Usando @Query BSON pura para máxima performance.
     */
    @Query("{ 'accountId' : ?0 }")
    List<CardDocument> findAllByAccountId(String accountId);

    /**
     * Busca um cartão específico garantindo que ele pertence àquela conta (Segurança BBA).
     */
    @Query("{ '_id' : ?0, 'accountId' : ?1 }")
    Optional<CardDocument> findByIdAndAccountId(String cardId, String accountId);
}
