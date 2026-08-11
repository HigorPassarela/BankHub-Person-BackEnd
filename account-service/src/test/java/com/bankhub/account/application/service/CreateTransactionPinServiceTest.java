package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("CreateTransactionPinService Unit Tests")
class CreateTransactionPinServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private CreateTransactionPinService createTransactionPinService;

    @Test
    @DisplayName("should create transaction PIN successfully")
    void shouldCreateTransactionPinSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .balance(Balance.zero())
                .build();

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(persistencePort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = createTransactionPinService.execute(accountId, customerId, plainPin);

        assertThat(result.transactionPinHash()).isNotNull();
        assertThat(result.transactionPinHash()).isNotEqualTo(plainPin);
    }

    @Test
    @DisplayName("should throw exception when PIN is invalid")
    void shouldThrowExceptionWhenPinInvalid() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String invalidPin = "12345";

        assertThatThrownBy(() -> createTransactionPinService.execute(accountId, customerId, invalidPin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("O PIN transacional deve conter exatamente 4 números");
    }

    @Test
    @DisplayName("should throw exception when PIN is null")
    void shouldThrowExceptionWhenPinNull() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        assertThatThrownBy(() -> createTransactionPinService.execute(accountId, customerId, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createTransactionPinService.execute(accountId, customerId, plainPin))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
