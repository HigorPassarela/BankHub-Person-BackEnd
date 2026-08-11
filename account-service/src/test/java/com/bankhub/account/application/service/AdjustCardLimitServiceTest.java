package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
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
import static org.mockito.Mockito.when;

@DisplayName("AdjustCardLimitService Unit Tests")
class AdjustCardLimitServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private CardPersistencePort cardPersistencePort;

    @InjectMocks
    private AdjustCardLimitService adjustCardLimitService;

    @Test
    @DisplayName("should adjust card limit successfully")
    void shouldAdjustCardLimitSuccessfully() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        BigDecimal newLimit = new BigDecimal("5000.00");

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .build();

        Card card = Card.builder()
                .id(cardId)
                .accountId(accountId)
                .type(CardType.PHYSICAL)
                .cardNumber("4111111111111111")
                .cardholderName("TEST USER")
                .expirationDate("12/28")
                .cvvHash("hash123")
                .isBlocked(false)
                .creditLimit(new BigDecimal("1000.00"))
                .availableLimit(new BigDecimal("1000.00"))
                .build();

        Card adjustedCard = card.adjustLimit(newLimit);

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));
        when(cardPersistencePort.save(any(Card.class))).thenReturn(adjustedCard);

        Card result = adjustCardLimitService.execute(accountId, cardId, customerId, newLimit);

        assertThat(result.creditLimit()).isEqualByComparingTo(newLimit);
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        BigDecimal newLimit = new BigDecimal("5000.00");

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adjustCardLimitService.execute(accountId, cardId, customerId, newLimit))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Conta não encontrada ou acesso negado");
    }

    @Test
    @DisplayName("should throw exception when card not found")
    void shouldThrowExceptionWhenCardNotFound() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        BigDecimal newLimit = new BigDecimal("5000.00");

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adjustCardLimitService.execute(accountId, cardId, customerId, newLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cartão não encontrado ou não pertence a esta conta");
    }
}
