package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.AccountTokenPort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActivateAccountService Unit Tests")
class ActivateAccountServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @Mock
    private AccountTokenPort tokenPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ActivateAccountService activateAccountService;

    @Test
    @DisplayName("should activate account successfully with valid token")
    void shouldActivateAccountSuccessfully() {
        String token = "valid-token";
        String accountId = "acc-123";

        Account pendingAccount = Account.builder()
                .id(accountId)
                .customerId("customer-001")
                .status(AccountStatus.PENDING_ACTIVATION)
                .balance(Balance.zero())
                .build();

        Account activatedAccount = pendingAccount.activate();

        when(tokenPort.resolveToken(token)).thenReturn(Optional.of(accountId));
        when(persistencePort.findById(accountId)).thenReturn(Optional.of(pendingAccount));
        when(persistencePort.save(any(Account.class))).thenReturn(activatedAccount);

        Account result = activateAccountService.execute(token);

        assertThat(result.status()).isEqualTo(AccountStatus.ACTIVE);
        verify(tokenPort).revokeToken(token);
    }

    @Test
    @DisplayName("should throw exception when token is invalid")
    void shouldThrowExceptionWhenTokenInvalid() {
        String invalidToken = "invalid-token";
        when(tokenPort.resolveToken(invalidToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activateAccountService.execute(invalidToken))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Token inválido ou expirado");
    }

    @Test
    @DisplayName("should throw exception when account is already activated")
    void shouldThrowExceptionWhenAccountAlreadyActivated() {
        String token = "valid-token";
        String accountId = "acc-123";

        Account activeAccount = Account.builder()
                .id(accountId)
                .customerId("customer-001")
                .status(AccountStatus.ACTIVE)
                .balance(Balance.zero())
                .build();

        when(tokenPort.resolveToken(token)).thenReturn(Optional.of(accountId));
        when(persistencePort.findById(accountId)).thenReturn(Optional.of(activeAccount));

        assertThatThrownBy(() -> activateAccountService.execute(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Esta conta já foi ativada");
    }
}
