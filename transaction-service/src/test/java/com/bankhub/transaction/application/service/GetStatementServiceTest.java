package com.bankhub.transaction.application.service;

import com.bankhub.transaction.base.BaseUnitTest;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import com.bankhub.transaction.infrastructure.mapper.TransactionMapper;
import com.bankhub.transaction.infrastructure.persistence.entity.TransactionDocument;
import com.bankhub.transaction.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("GetStatementService Unit Tests")
class GetStatementServiceTest extends BaseUnitTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private GetStatementService getStatementService;

    @Test
    @DisplayName("should retrieve statement successfully with transactions sorted by date descending")
    void shouldRetrieveStatementSuccessfully() {
        // Arrange
        String accountId = "acc-123";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);

        TransactionDocument doc1 = createTransactionDocument("txn-1", accountId, twoDaysAgo);
        TransactionDocument doc2 = createTransactionDocument("txn-2", accountId, yesterday);
        TransactionDocument doc3 = createTransactionDocument("txn-3", accountId, now);

        Transaction transaction1 = createTransaction("txn-1", accountId, twoDaysAgo);
        Transaction transaction2 = createTransaction("txn-2", accountId, yesterday);
        Transaction transaction3 = createTransaction("txn-3", accountId, now);

        when(repository.fetchStatementByAccountId(accountId, accountId))
                .thenReturn(Arrays.asList(doc1, doc2, doc3));

        when(mapper.toDomain(doc1)).thenReturn(transaction1);
        when(mapper.toDomain(doc2)).thenReturn(transaction2);
        when(mapper.toDomain(doc3)).thenReturn(transaction3);

        // Act
        List<Transaction> result = getStatementService.execute(accountId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo("txn-3"); // Most recent first
        assertThat(result.get(1).id()).isEqualTo("txn-2");
        assertThat(result.get(2).id()).isEqualTo("txn-1");
        assertThat(result.get(0).createdAt()).isAfter(result.get(1).createdAt());
        assertThat(result.get(1).createdAt()).isAfter(result.get(2).createdAt());
    }

    @Test
    @DisplayName("should return empty list when no transactions exist for account")
    void shouldReturnEmptyListWhenNoTransactions() {
        // Arrange
        String accountId = "acc-empty";

        when(repository.fetchStatementByAccountId(accountId, accountId))
                .thenReturn(Collections.emptyList());

        // Act
        List<Transaction> result = getStatementService.execute(accountId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should handle single transaction in statement")
    void shouldHandleSingleTransaction() {
        // Arrange
        String accountId = "acc-123";
        LocalDateTime now = LocalDateTime.now();

        TransactionDocument doc = createTransactionDocument("txn-1", accountId, now);
        Transaction transaction = createTransaction("txn-1", accountId, now);

        when(repository.fetchStatementByAccountId(accountId, accountId))
                .thenReturn(Collections.singletonList(doc));

        when(mapper.toDomain(doc)).thenReturn(transaction);

        // Act
        List<Transaction> result = getStatementService.execute(accountId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("txn-1");
    }

    @Test
    @DisplayName("should correctly sort transactions with same date but different times")
    void shouldSortTransactionsWithSameDateDifferentTimes() {
        // Arrange
        String accountId = "acc-123";
        LocalDateTime baseTime = LocalDateTime.of(2026, 8, 10, 10, 0);

        TransactionDocument doc1 = createTransactionDocument("txn-1", accountId, baseTime);
        TransactionDocument doc2 = createTransactionDocument("txn-2", accountId, baseTime.plusHours(1));
        TransactionDocument doc3 = createTransactionDocument("txn-3", accountId, baseTime.plusHours(2));

        Transaction transaction1 = createTransaction("txn-1", accountId, baseTime);
        Transaction transaction2 = createTransaction("txn-2", accountId, baseTime.plusHours(1));
        Transaction transaction3 = createTransaction("txn-3", accountId, baseTime.plusHours(2));

        when(repository.fetchStatementByAccountId(accountId, accountId))
                .thenReturn(Arrays.asList(doc1, doc2, doc3));

        when(mapper.toDomain(doc1)).thenReturn(transaction1);
        when(mapper.toDomain(doc2)).thenReturn(transaction2);
        when(mapper.toDomain(doc3)).thenReturn(transaction3);

        // Act
        List<Transaction> result = getStatementService.execute(accountId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo("txn-3"); // Latest time first
        assertThat(result.get(1).id()).isEqualTo("txn-2");
        assertThat(result.get(2).id()).isEqualTo("txn-1");
    }

    private TransactionDocument createTransactionDocument(String id, String accountId, LocalDateTime createdAt) {
        TransactionDocument doc = new TransactionDocument();
        doc.setId(id);
        doc.setSourceAccountId(accountId);
        doc.setDestinationAccountId("dest-" + accountId);
        doc.setAmount(new BigDecimal("100.00"));
        doc.setType(TransactionType.INTERNAL_TRANSFER);
        doc.setStatus(TransactionStatus.COMPLETED);
        doc.setCategory(TransactionCategory.TRANSFER);
        doc.setCreatedAt(createdAt);
        return doc;
    }

    private Transaction createTransaction(String id, String accountId, LocalDateTime createdAt) {
        return Transaction.builder()
                .id(id)
                .sourceAccountId(accountId)
                .destinationAccountId("dest-" + accountId)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .category(TransactionCategory.TRANSFER)
                .createdAt(createdAt)
                .build();
    }
}
