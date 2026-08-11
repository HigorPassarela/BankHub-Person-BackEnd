package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import com.bankhub.account.domain.exception.InsufficientFundsException;
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

@DisplayName("DebitAccountService Unit Tests")
class DebitAccountServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private DebitAccountService debitAccountService;

    @Test
    @DisplayName("should debit account successfully")
    void shouldDebitAccountSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        BigDecimal debitAmount = new BigDecimal("50.00");

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("100.00"), "BRL"))
                .build();

        Account debitedAccount = account.debit(debitAmount);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(persistencePort.save(any(Account.class))).thenReturn(debitedAccount);

        Account result = debitAccountService.execute(accountId, customerId, debitAmount);

        assertThat(result.balance().amount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        BigDecimal debitAmount = new BigDecimal("150.00");

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .balance(new Balance(new BigDecimal("100.00"), "BRL"))
                .build();

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> debitAccountService.execute(accountId, customerId, debitAmount))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("should throw exception when account not active")
    void shouldThrowExceptionWhenAccountNotActive() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        BigDecimal debitAmount = new BigDecimal("50.00");

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.BLOCKED)
                .balance(new Balance(new BigDecimal("100.00"), "BRL"))
                .build();

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> debitAccountService.execute(accountId, customerId, debitAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Débitos só são permitidos em contas ativas");
    }
}
