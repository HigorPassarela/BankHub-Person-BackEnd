package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.AccountTokenPort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountPersistencePort persistencePort;
    @Mock
    private AccountTokenPort tokenPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateAccountService createAccountService;

    @Test
    @DisplayName("Deve criar uma nova conta com status PENDING_ACTIVATION e gerar token")
    void shouldCreateAccountSuccessfully() {
        // Arrange
        String customerId = "customer-123";
        String fullName = "Higor Passarela";
        
        when(persistencePort.save(any(Account.class))).thenAnswer(i -> {
            Account acc = i.getArgument(0);
            return acc.toBuilder().id("mock-id-123").build(); // Simula o retorno do MongoDB
        });
        when(tokenPort.generateAndSaveToken("mock-id-123")).thenReturn("mock-token-uuid");

        // Act
        Account result = createAccountService.execute(customerId, fullName, "11999999999", "Rua A");

        // Assert
        assertNotNull(result);
        assertEquals(AccountStatus.PENDING_ACTIVATION, result.status());
        assertEquals("mock-id-123", result.id());
        
        verify(persistencePort, times(1)).save(any(Account.class));
        verify(tokenPort, times(1)).generateAndSaveToken("mock-id-123");
        verify(eventPublisher, times(1)).publishEvent(any());
    }
}
