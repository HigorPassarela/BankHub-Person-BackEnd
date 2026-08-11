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
import static org.mockito.Mockito.when;

@DisplayName("FindAccountService Unit Tests")
class FindAccountServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private FindAccountService findAccountService;

    @Test
    @DisplayName("should find account successfully")
    void shouldFindAccountSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .balance(Balance.zero())
                .build();

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        Account result = findAccountService.execute(accountId, customerId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(accountId);
        assertThat(result.customerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> findAccountService.execute(accountId, customerId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Conta não encontrada ou acesso negado");
    }
}
