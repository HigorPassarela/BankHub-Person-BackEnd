package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.out.TransactionEventPublisherPort;
import com.bankhub.transaction.application.port.out.TransactionPersistencePort;
import com.bankhub.transaction.base.BaseUnitTest;
import com.bankhub.transaction.domain.Transaction;
import com.bankhub.transaction.domain.TransactionCategory;
import com.bankhub.transaction.domain.TransactionStatus;
import com.bankhub.transaction.domain.TransactionType;
import com.bankhub.transaction.infrastructure.client.AccountFeignClient;
import com.bankhub.transaction.infrastructure.client.dto.PinValidationRequest;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("InitiatePixService Unit Tests")
class InitiatePixServiceTest extends BaseUnitTest {

    @Mock
    private TransactionPersistencePort persistencePort;

    @Mock
    private TransactionEventPublisherPort eventPublisherPort;

    @Mock
    private AccountFeignClient accountFeignClient;

    @InjectMocks
    private InitiatePixService initiatePixService;

    @Test
    @DisplayName("should initiate PIX transfer successfully when all validations pass")
    void shouldInitiatePixTransferSuccessfully() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";
        String category = "TRANSFER";

        Transaction savedTransaction = Transaction.builder()
                .id("txn-123")
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.TRANSFER)
                .build();

        when(persistencePort.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("txn-123");
        assertThat(result.sourceAccountId()).isEqualTo(sourceAccountId);
        assertThat(result.destinationAccountId()).isEqualTo(destinationAccountId);
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(result.category()).isEqualTo(TransactionCategory.TRANSFER);

        verify(accountFeignClient).validateTransaction(eq(sourceAccountId), eq(customerId), any(PinValidationRequest.class));
        verify(persistencePort).save(any(Transaction.class));
        verify(eventPublisherPort).publishTransactionInitiatedEvent(savedTransaction);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when source and destination accounts are the same")
    void shouldFailWhenSourceEqualsDestination() {
        // Arrange
        String customerId = "customer-001";
        String accountId = "acc-same";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";
        String category = "TRANSFER";

        // Act & Assert
        assertThatThrownBy(() ->
                initiatePixService.execute(customerId, accountId, accountId, amount, transactionPin, category)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não é possível realizar uma transferência para a própria conta");

        verifyNoInteractions(accountFeignClient);
        verifyNoInteractions(persistencePort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("should throw SecurityException when PIN validation fails (Forbidden)")
    void shouldFailWhenPinValidationFails() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "wrong-pin";
        String category = "TRANSFER";

        Request request = Request.create(Request.HttpMethod.POST, "/api/v1/accounts/validate", new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.Forbidden("PIN incorrect", request, null, null))
                .when(accountFeignClient).validateTransaction(anyString(), anyString(), any(PinValidationRequest.class));

        // Act & Assert
        assertThatThrownBy(() ->
                initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, category)
        )
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Transação negada: A sua senha está incorreta ou o KYC (Selfie) está pendente");

        verify(accountFeignClient).validateTransaction(eq(sourceAccountId), eq(customerId), any(PinValidationRequest.class));
        verifyNoInteractions(persistencePort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("should throw SecurityException when account is not found (NotFound)")
    void shouldFailWhenAccountNotFound() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-nonexistent";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";
        String category = "TRANSFER";

        Request request = Request.create(Request.HttpMethod.POST, "/api/v1/accounts/validate", new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.NotFound("Account not found", request, null, null))
                .when(accountFeignClient).validateTransaction(anyString(), anyString(), any(PinValidationRequest.class));

        // Act & Assert
        assertThatThrownBy(() ->
                initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, category)
        )
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Transação negada: A sua senha está incorreta ou o KYC (Selfie) está pendente");

        verify(accountFeignClient).validateTransaction(eq(sourceAccountId), eq(customerId), any(PinValidationRequest.class));
        verifyNoInteractions(persistencePort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("should throw IllegalStateException when account service is unavailable")
    void shouldFailWhenAccountServiceUnavailable() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";
        String category = "TRANSFER";

        doThrow(new RuntimeException("Service unavailable"))
                .when(accountFeignClient).validateTransaction(anyString(), anyString(), any(PinValidationRequest.class));

        // Act & Assert
        assertThatThrownBy(() ->
                initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, category)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("O serviço de validação do banco está indisponível");

        verify(accountFeignClient).validateTransaction(eq(sourceAccountId), eq(customerId), any(PinValidationRequest.class));
        verifyNoInteractions(persistencePort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    @DisplayName("should default to OTHER category when invalid category is provided")
    void shouldDefaultToOtherWhenInvalidCategory() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";
        String invalidCategory = "INVALID_CATEGORY";

        Transaction savedTransaction = Transaction.builder()
                .id("txn-123")
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.OTHER)
                .build();

        when(persistencePort.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, invalidCategory);

        // Assert
        assertThat(result.category()).isEqualTo(TransactionCategory.OTHER);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().category()).isEqualTo(TransactionCategory.OTHER);
    }

    @Test
    @DisplayName("should default to OTHER category when category is null")
    void shouldDefaultToOtherWhenCategoryIsNull() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";

        Transaction savedTransaction = Transaction.builder()
                .id("txn-123")
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.OTHER)
                .build();

        when(persistencePort.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, null);

        // Assert
        assertThat(result.category()).isEqualTo(TransactionCategory.OTHER);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().category()).isEqualTo(TransactionCategory.OTHER);
    }

    @Test
    @DisplayName("should default to OTHER category when category is blank")
    void shouldDefaultToOtherWhenCategoryIsBlank() {
        // Arrange
        String customerId = "customer-001";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-destination";
        BigDecimal amount = new BigDecimal("100.00");
        String transactionPin = "1234";

        Transaction savedTransaction = Transaction.builder()
                .id("txn-123")
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.INTERNAL_TRANSFER)
                .status(TransactionStatus.PENDING)
                .category(TransactionCategory.OTHER)
                .build();

        when(persistencePort.save(any(Transaction.class))).thenReturn(savedTransaction);

        // Act
        Transaction result = initiatePixService.execute(customerId, sourceAccountId, destinationAccountId, amount, transactionPin, "   ");

        // Assert
        assertThat(result.category()).isEqualTo(TransactionCategory.OTHER);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(persistencePort).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().category()).isEqualTo(TransactionCategory.OTHER);
    }
}
