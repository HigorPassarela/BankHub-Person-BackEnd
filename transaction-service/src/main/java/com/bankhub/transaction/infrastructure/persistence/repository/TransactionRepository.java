package com.bankhub.transaction.infrastructure.persistence.repository;

import com.bankhub.transaction.infrastructure.persistence.entity.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<TransactionDocument, String> {

    @Query("{ $or: [ { 'sourceAccountId' : ?0 }, { 'destinationAccountId' : ?1 } ] }")
    List<TransactionDocument> fetchStatementByAccountId(String sourceAccountId, String destinationAccountId);
}
