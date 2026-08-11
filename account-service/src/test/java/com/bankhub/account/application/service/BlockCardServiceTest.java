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

@DisplayName("BlockCardService Unit Tests")
class BlockCardServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort accountPersistencePort;

    @Mock
    private CardPersistencePort cardPersistencePort;

    @InjectMocks
    private BlockCardService blockCardService;

    @Test
    @DisplayName("should block card successfully")
    void shouldBlockCardSuccessfully() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";

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
                .build();

        Card blockedCard = card.toggleBlock();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));
        when(cardPersistencePort.save(any(Card.class))).thenReturn(blockedCard);

        Card result = blockCardService.execute(accountId, cardId, customerId);

        assertThat(result.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("should unblock card successfully")
    void shouldUnblockCardSuccessfully() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";

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
                .isBlocked(true)
                .creditLimit(new BigDecimal("1000.00"))
                .build();

        Card unblockedCard = card.toggleBlock();

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));
        when(cardPersistencePort.save(any(Card.class))).thenReturn(unblockedCard);

        Card result = blockCardService.execute(accountId, cardId, customerId);

        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";

        when(accountPersistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blockCardService.execute(accountId, cardId, customerId))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
