package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.AccountTokenPort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CreateAccountService Unit Tests")
class CreateAccountServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;
    @Mock
    private AccountTokenPort tokenPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateAccountService createAccountService;

    @Test
    @DisplayName("should create account successfully with pending activation status")
    void shouldCreateAccountSuccessfully() {
        String customerId = "customer-123";
        String fullName = "Higor Passarela";
        String phone = "11999999999";
        String address = "Rua A";

        when(persistencePort.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return account.toBuilder().id("acc-123").build();
        });
        when(tokenPort.generateAndSaveToken(anyString())).thenReturn("activation-token-123");

        Account result = createAccountService.execute(customerId, fullName, phone, address);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("acc-123");
        assertThat(result.customerId()).isEqualTo(customerId);
        assertThat(result.fullName()).isEqualTo(fullName);
        assertThat(result.status()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
        verify(persistencePort).save(any(Account.class));
        verify(tokenPort).generateAndSaveToken("acc-123");
    }
}
