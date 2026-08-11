package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.base.BaseUnitTest;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CompletePixService Unit Tests")
class CompletePixServiceTest extends BaseUnitTest {

    @Mock
    private TransactionPersistencePort persistencePort;

    @InjectMocks
    private CompletePixService completePixService;

    @Test
    @DisplayName("should complete PIX transaction successfully when saga status is COMPLETED")
    void shouldCompleteTransactionSuccessfully() {
        // Arrange
        String transactionId = "txn-123";
        String sagaStatus = "COMPLETED";
        String failureReason = null;

        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.findById(transactionId)).thenReturn(Optional.of(pendingTransaction));

        // Act
        completePixService.execute(transactionId, sagaStatus, failureReason);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("should fail PIX transaction when saga status is not COMPLETED")
    void shouldFailTransactionWhenSagaFailed() {
        // Arrange
        String transactionId = "txn-123";
        String sagaStatus = "FAILED";
        String failureReason = "Insufficient funds";

        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.findById(transactionId)).thenReturn(Optional.of(pendingTransaction));

        // Act
        completePixService.execute(transactionId, sagaStatus, failureReason);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(savedTransaction.failureReason()).isEqualTo(failureReason);
    }

    @Test
    @DisplayName("should use default failure message when failureReason is OK")
    void shouldUseDefaultFailureMessageWhenReasonIsOk() {
        // Arrange
        String transactionId = "txn-123";
        String sagaStatus = "FAILED";
        String failureReason = "OK";

        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.findById(transactionId)).thenReturn(Optional.of(pendingTransaction));

        // Act
        completePixService.execute(transactionId, sagaStatus, failureReason);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(savedTransaction.failureReason()).isEqualTo("Falha interna no motor de contas");
    }

    @Test
    @DisplayName("should throw IllegalStateException when transaction is not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Arrange
        String transactionId = "txn-nonexistent";
        String sagaStatus = "COMPLETED";
        String failureReason = null;

        when(persistencePort.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                completePixService.execute(transactionId, sagaStatus, failureReason)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transação não encontrada para fechamento da Saga");

        verify(persistencePort).findById(transactionId);
    }

    @Test
    @DisplayName("should handle different failure reasons correctly")
    void shouldHandleDifferentFailureReasons() {
        // Arrange
        String transactionId = "txn-123";
        String sagaStatus = "FAILED";
        String failureReason = "Account blocked by fraud detection";

        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.findById(transactionId)).thenReturn(Optional.of(pendingTransaction));

        // Act
        completePixService.execute(transactionId, sagaStatus, failureReason);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(savedTransaction.failureReason()).isEqualTo("Account blocked by fraud detection");
    }

    @Test
    @DisplayName("should handle null failure reason for failed saga")
    void shouldHandleNullFailureReasonForFailedSaga() {
        // Arrange
        String transactionId = "txn-123";
        String sagaStatus = "FAILED";
        String failureReason = null;

        Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .sourceAccountId("acc-source")
                .destinationAccountId("acc-dest")
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.findById(transactionId)).thenReturn(Optional.of(pendingTransaction));

        // Act
        completePixService.execute(transactionId, sagaStatus, failureReason);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.status()).isEqualTo(TransactionStatus.FAILED);
        // When failureReason is null, it's passed through as-is (not replaced)
        assertThat(savedTransaction.failureReason()).isNull();
    }
}
