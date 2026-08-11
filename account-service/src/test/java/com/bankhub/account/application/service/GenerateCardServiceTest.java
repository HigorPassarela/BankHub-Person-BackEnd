package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.CardCachePort;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.CardType;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("GenerateCardService Unit Tests")
class GenerateCardServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private CardPersistencePort cardPersistencePort;

    @Mock
    private CardCachePort cardCachePort;

    @InjectMocks
    private GenerateCardService generateCardService;

    @Test
    @DisplayName("should generate physical card successfully")
    void shouldGeneratePhysicalCardSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        String physicalPin = "4321";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return card.toBuilder().id("card-123").build();
        });

        Card result = generateCardService.execute(accountId, customerId, "PHYSICAL", physicalPin);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(CardType.PHYSICAL);
        assertThat(result.cardNumber()).hasSize(16);
    }

    @Test
    @DisplayName("should generate temporary card successfully")
    void shouldGenerateTemporaryCardSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            return card.toBuilder().id("card-temp-123").build();
        });

        Card result = generateCardService.execute(accountId, customerId, "TEMPORARY", null);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(CardType.TEMPORARY);
        verify(cardCachePort).registerTemporaryCard(anyString());
    }

    @Test
    @DisplayName("should throw exception when account not active")
    void shouldThrowExceptionWhenAccountNotActive() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.PENDING_ACTIVATION)
                .build();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> generateCardService.execute(accountId, customerId, "PHYSICAL", "1234"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Emissão de cartões só é permitida para contas ativas");
    }

    @Test
    @DisplayName("should throw exception when physical card without PIN")
    void shouldThrowExceptionWhenPhysicalCardWithoutPin() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> generateCardService.execute(accountId, customerId, "PHYSICAL", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A senha física (4 dígitos) é obrigatória");
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generateCardService.execute(accountId, customerId, "PHYSICAL", "1234"))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
