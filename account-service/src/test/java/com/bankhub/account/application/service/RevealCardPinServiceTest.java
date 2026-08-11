package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ValidateTransactionPinUseCase;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.CardType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RevealCardPinService Unit Tests")
class RevealCardPinServiceTest extends BaseUnitTest {

    @Mock
    private ValidateTransactionPinUseCase validateTransactionPinUseCase;

    @Mock
    private CardPersistencePort cardPersistencePort;

    @InjectMocks
    private RevealCardPinService revealCardPinService;

    @Test
    @DisplayName("should reveal card PIN successfully")
    void shouldRevealCardPinSuccessfully() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        String transactionPin = "1234";

        Card card = Card.builder()
                .id(cardId)
                .accountId(accountId)
                .type(CardType.PHYSICAL)
                .cardNumber("4111111111111111")
                .cardholderName("TEST USER")
                .expirationDate("12/28")
                .cvvHash("hash123")
                .isBlocked(false)
                .physicalPinHash("hashed-pin")
                .creditLimit(new BigDecimal("1000.00"))
                .build();

        when(validateTransactionPinUseCase.execute(accountId, customerId, transactionPin)).thenReturn(true);
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));

        String revealedPin = revealCardPinService.execute(accountId, cardId, customerId, transactionPin);

        assertThat(revealedPin).isNotNull();
        assertThat(revealedPin).isEqualTo("1234");
        verify(validateTransactionPinUseCase).execute(accountId, customerId, transactionPin);
    }

    @Test
    @DisplayName("should throw exception when card is blocked")
    void shouldThrowExceptionWhenCardBlocked() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        String transactionPin = "1234";

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

        when(validateTransactionPinUseCase.execute(accountId, customerId, transactionPin)).thenReturn(true);
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> revealCardPinService.execute(accountId, cardId, customerId, transactionPin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível exibir a senha de um cartão bloqueado");
    }

    @Test
    @DisplayName("should throw exception when card is not physical")
    void shouldThrowExceptionWhenCardNotPhysical() {
        String accountId = "acc-123";
        String cardId = "card-123";
        String customerId = "customer-001";
        String transactionPin = "1234";

        Card card = Card.builder()
                .id(cardId)
                .accountId(accountId)
                .type(CardType.TEMPORARY)
                .cardNumber("4111111111111111")
                .cardholderName("TEST USER")
                .expirationDate("12/28")
                .cvvHash("hash123")
                .isBlocked(false)
                .creditLimit(new BigDecimal("1000.00"))
                .build();

        when(validateTransactionPinUseCase.execute(accountId, customerId, transactionPin)).thenReturn(true);
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> revealCardPinService.execute(accountId, cardId, customerId, transactionPin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Apenas cartões físicos possuem senha");
    }

    @Test
    @DisplayName("should throw exception when card not found")
    void shouldThrowExceptionWhenCardNotFound() {
        String accountId = "acc-123";
        String cardId = "card-invalid";
        String customerId = "customer-001";
        String transactionPin = "1234";

        when(validateTransactionPinUseCase.execute(accountId, customerId, transactionPin)).thenReturn(true);
        when(cardPersistencePort.findByIdAndAccountId(cardId, accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> revealCardPinService.execute(accountId, cardId, customerId, transactionPin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cartão não encontrado");
    }
}
