package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("ResolveAccountDictService Unit Tests")
class ResolveAccountDictServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private ResolveAccountDictService resolveAccountDictService;

    @Test
    @DisplayName("should resolve account by number successfully")
    void shouldResolveAccountSuccessfully() {
        String accountNumber = "0001-ABC123-1";

        Account account = Account.builder()
                .id("acc-123")
                .customerId("customer-001")
                .status(AccountStatus.ACTIVE)
                .balance(Balance.zero())
                .build();

        when(persistencePort.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        Account result = resolveAccountDictService.execute(accountNumber);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("acc-123");
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountNumber = "0001-INVALID-1";

        when(persistencePort.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolveAccountDictService.execute(accountNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chave PIX inválida");
    }

    @Test
    @DisplayName("should throw exception when account not active")
    void shouldThrowExceptionWhenAccountNotActive() {
        String accountNumber = "0001-ABC123-1";

        Account account = Account.builder()
                .id("acc-123")
                .customerId("customer-001")
                .status(AccountStatus.BLOCKED)
                .balance(Balance.zero())
                .build();

        when(persistencePort.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> resolveAccountDictService.execute(accountNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Esta conta não está apta a receber transferências");
    }
}
