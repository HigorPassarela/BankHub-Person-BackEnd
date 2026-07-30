package com.bankhub.account.infrastructure.persistence.repository;

import com.bankhub.account.infrastructure.persistence.entity.AccountDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<AccountDocument, String> {

    List<AccountDocument> findByCustomerId(String customerId);

    Optional<AccountDocument> findByIdAndCustomerId(String id, String customerId);

    @Query("{ 'accountNumber.number' : ?0 }")
    Optional<AccountDocument> findByAccountNumber(String number);
}
