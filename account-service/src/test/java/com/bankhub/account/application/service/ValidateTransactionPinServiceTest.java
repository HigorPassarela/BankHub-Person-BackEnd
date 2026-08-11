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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("ValidateTransactionPinService Unit Tests")
class ValidateTransactionPinServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ValidateTransactionPinService validateTransactionPinService;

    @Test
    @DisplayName("should validate transaction PIN successfully")
    void shouldValidateTransactionPinSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPin = encoder.encode(plainPin);

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .transactionPinHash(hashedPin)
                .balance(Balance.zero())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        boolean result = validateTransactionPinService.execute(accountId, customerId, plainPin);

        assertThat(result).isTrue();
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("should throw exception when PIN is incorrect")
    void shouldThrowExceptionWhenPinIncorrect() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";
        String wrongPin = "4321";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPin = encoder.encode(plainPin);

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .transactionPinHash(hashedPin)
                .balance(Balance.zero())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> validateTransactionPinService.execute(accountId, customerId, wrongPin))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("A assinatura eletrônica (PIN) fornecida está incorreta");

        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("should throw exception when max attempts reached")
    void shouldThrowExceptionWhenMaxAttemptsReached() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("3");

        assertThatThrownBy(() -> validateTransactionPinService.execute(accountId, customerId, plainPin))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Conta temporariamente bloqueada");
    }

    @Test
    @DisplayName("should throw exception when PIN not set")
    void shouldThrowExceptionWhenPinNotSet() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .transactionPinHash(null)
                .balance(Balance.zero())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> validateTransactionPinService.execute(accountId, customerId, plainPin))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Nenhum PIN de segurança cadastrado");
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String plainPin = "1234";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validateTransactionPinService.execute(accountId, customerId, plainPin))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
