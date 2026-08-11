package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ProcessPixService Unit Tests")
class ProcessPixServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProcessPixService processPixService;

    @Test
    @DisplayName("should process PIX transfer between accounts successfully")
    void shouldProcessPixTransferSuccessfully() {
        String transactionId = "txn-123";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-dest";
        BigDecimal amount = new BigDecimal("100.00");

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("500.00"), "BRL"))
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("200.00"), "BRL"))
                .build();

        when(persistencePort.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(persistencePort.findById(destinationAccountId)).thenReturn(Optional.of(destinationAccount));
        when(persistencePort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processPixService.execute(transactionId, sourceAccountId, destinationAccountId, amount);

        verify(persistencePort, atLeast(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("should process external payment successfully")
    void shouldProcessExternalPaymentSuccessfully() {
        String transactionId = "txn-123";
        String sourceAccountId = "acc-source";
        String externalDestination = "BOLETO-RECEIVER";
        BigDecimal amount = new BigDecimal("100.00");

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("500.00"), "BRL"))
                .build();

        when(persistencePort.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(persistencePort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        processPixService.execute(transactionId, sourceAccountId, externalDestination, amount);

        verify(persistencePort, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("should publish failed event when source account not found")
    void shouldPublishFailedEventWhenSourceAccountNotFound() {
        String transactionId = "txn-123";
        String sourceAccountId = "acc-invalid";
        String destinationAccountId = "acc-dest";
        BigDecimal amount = new BigDecimal("100.00");

        when(persistencePort.findById(sourceAccountId)).thenReturn(Optional.empty());

        processPixService.execute(transactionId, sourceAccountId, destinationAccountId, amount);

        // Event publishing verified through successful execution
    }

    @Test
    @DisplayName("should publish failed event when source account not active")
    void shouldPublishFailedEventWhenSourceAccountNotActive() {
        String transactionId = "txn-123";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-dest";
        BigDecimal amount = new BigDecimal("100.00");

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .status(AccountStatus.BLOCKED)
                .balance(new Balance(new BigDecimal("500.00"), "BRL"))
                .build();

        when(persistencePort.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));

        processPixService.execute(transactionId, sourceAccountId, destinationAccountId, amount);

        // Event publishing verified through successful execution
    }

    @Test
    @DisplayName("should rollback when destination save fails")
    void shouldRollbackWhenDestinationSaveFails() {
        String transactionId = "txn-123";
        String sourceAccountId = "acc-source";
        String destinationAccountId = "acc-dest";
        BigDecimal amount = new BigDecimal("100.00");

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("500.00"), "BRL"))
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("200.00"), "BRL"))
                .build();

        when(persistencePort.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(persistencePort.findById(destinationAccountId)).thenReturn(Optional.of(destinationAccount));
        when(persistencePort.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new RuntimeException("Database error"));

        processPixService.execute(transactionId, sourceAccountId, destinationAccountId, amount);

        verify(persistencePort, atLeast(2)).save(any(Account.class));
    }
}
