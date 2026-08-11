package com.bankhub.transaction.integration;

import com.bankhub.transaction.base.BaseIntegrationTest;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import com.bankhub.transaction.infrastructure.adapter.out.persistence.TransactionPersistenceAdapter;
import com.bankhub.transaction.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Integration tests require Docker - run separately")
@DisplayName("TransactionPersistenceAdapter Integration Tests")
class TransactionPersistenceAdapterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionPersistenceAdapter persistenceAdapter;

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("should save transaction to MongoDB successfully")
    void shouldSaveTransactionSuccessfully() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        // Act
        Transaction saved = persistenceAdapter.save(transaction);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isNotNull();
        assertThat(saved.sourceAccountId()).isEqualTo("acc-source");
        assertThat(saved.destinationAccountId()).isEqualTo("acc-dest");
        assertThat(saved.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(saved.status()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("should find transaction by ID successfully")
    void shouldFindTransactionByIdSuccessfully() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        Transaction saved = persistenceAdapter.save(transaction);

        // Act
        Optional<Transaction> found = persistenceAdapter.findById(saved.id());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(saved.id());
        assertThat(found.get().sourceAccountId()).isEqualTo("acc-source");
    }

    @Test
    @DisplayName("should return empty when transaction not found")
    void shouldReturnEmptyWhenTransactionNotFound() {
        // Act
        Optional<Transaction> found = persistenceAdapter.findById("nonexistent-id");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should update existing transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        Transaction saved = persistenceAdapter.save(transaction);

        // Act - update transaction to completed
        Transaction completed = saved.complete();
        Transaction updated = persistenceAdapter.save(completed);

        // Assert
        assertThat(updated.id()).isEqualTo(saved.id());
        assertThat(updated.status()).isEqualTo(TransactionStatus.COMPLETED);

        Optional<Transaction> found = persistenceAdapter.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("should persist all transaction fields correctly")
    void shouldPersistAllFieldsCorrectly() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("250.75"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.FOOD)
                .build();

        // Act
        Transaction saved = persistenceAdapter.save(transaction);
        Optional<Transaction> found = persistenceAdapter.findById(saved.id());

        // Assert
        assertThat(found).isPresent();
        Transaction retrieved = found.get();
        assertThat(retrieved.sourceAccountId()).isEqualTo("acc-source");
        assertThat(retrieved.destinationAccountId()).isEqualTo("acc-dest");
        assertThat(retrieved.amount()).isEqualByComparingTo(new BigDecimal("250.75"));
        assertThat(retrieved.type()).isEqualTo(TransactionType.INTERNAL_TRANSFER);
        assertThat(retrieved.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(retrieved.category()).isEqualTo(TransactionCategory.FOOD);
        assertThat(retrieved.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("should persist failed transaction with failure reason")
    void shouldPersistFailedTransactionWithReason() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        Transaction saved = persistenceAdapter.save(transaction);

        // Act
        Transaction failed = saved.fail("Insufficient funds");
        Transaction updated = persistenceAdapter.save(failed);

        // Assert
        Optional<Transaction> found = persistenceAdapter.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(found.get().failureReason()).isEqualTo("Insufficient funds");
    }
}
