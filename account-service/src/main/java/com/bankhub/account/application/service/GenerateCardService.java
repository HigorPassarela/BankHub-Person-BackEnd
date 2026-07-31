package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.GenerateCardUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.application.port.out.CardCachePort;
import com.bankhub.account.application.port.out.CardPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Card;
import com.bankhub.account.domain.CardType;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateCardService implements GenerateCardUseCase {

    private final AccountPersistencePort accountPersistencePort;
    private final CardPersistencePort cardPersistencePort;
    private final CardCachePort cardCachePort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Card execute(String accountId, String customerId, String typeStr, String physicalPin) {
        log.info("Iniciando emissão de Cartão [{}] para a conta: {}", typeStr, accountId);

        CardType type = CardType.valueOf(typeStr.toUpperCase());

        Account account = accountPersistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta corrente não encontrada ou acesso negado."));

        if (account.status() != AccountStatus.ACTIVE) {
            log.warn("Tentativa de emissão de cartão negada. Conta {} está com status: {}", accountId, account.status());
            throw new IllegalStateException("Emissão de cartões só é permitida para contas ativas e regulares.");
        }

        String hashedPin = null;
        if (type == CardType.PHYSICAL) {
            if (physicalPin == null || !physicalPin.matches("^\\d{4}$")) {
                throw new IllegalArgumentException("A senha física (4 dígitos) é obrigatória para emissão do cartão físico.");
            }
            hashedPin = passwordEncoder.encode(physicalPin);
        }

        String generatedPAN = generate16DigitPan();
        String generatedCVV = generateCvv();
        String hashedCvv = passwordEncoder.encode(generatedCVV);
        String expiration = generateExpirationDate();

        BigDecimal baseLimit = new BigDecimal("1000.00");

        Card newCard = Card.builder()
                .accountId(account.id())
                .type(type)
                .cardNumber(generatedPAN)
                .cardholderName("CLIENTE VIP " + customerId.substring(0, 4).toUpperCase())
                .expirationDate(expiration)
                .cvvHash(hashedCvv)
                .physicalPinHash(hashedPin)
                .isBlocked(true)
                .nfcEnabled(true)
                .onlinePurchasesEnabled(type != CardType.PHYSICAL)
                .internationalUsageEnabled(false)
                .creditLimit(baseLimit)
                .availableLimit(baseLimit)
                .build();

        Card savedCard = cardPersistencePort.save(newCard);

        if (type == CardType.TEMPORARY) {
            cardCachePort.registerTemporaryCard(savedCard.id());
        }

        log.info("Cartão emitido com sucesso! ID: {}. Máscara: {}", savedCard.id(), savedAccountPan(generatedPAN));
        return savedCard.toBuilder().cvvHash(generatedCVV).build();
    }

    private String generate16DigitPan() {
        Random random = new Random();
        StringBuilder pan = new StringBuilder("4");
        for (int i = 0; i < 15; i++) {
            pan.append(random.nextInt(10));
        }
        return pan.toString();
    }

    private String generateCvv() {
        return String.format("%03d", new Random().nextInt(1000));
    }

    private String generateExpirationDate() {
        return LocalDateTime.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    private String savedAccountPan(String pan) {
        return "•••• •••• •••• " + pan.substring(12, 16);
    }
}
