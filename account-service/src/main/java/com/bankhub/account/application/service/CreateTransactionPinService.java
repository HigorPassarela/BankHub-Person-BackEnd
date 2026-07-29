package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.CreateTransactionPinUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTransactionPinService implements CreateTransactionPinUseCase {

    private final AccountPersistencePort persistencePort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Account execute(String accountId, String customerId, String plainPin) {
        log.info("Iniciando processo de cadastro de PIN transacional para a conta: {}. Cliente: {}", accountId, customerId);

        if (plainPin == null || !plainPin.matches("^\\d{4}$")) {
            log.warn("Tentativa de criação de PIN com formato inválido (deve conter exatos 4 dígitos). Conta: {}", accountId);
            throw new IllegalArgumentException("O PIN transacional deve conter exatamente 4 números.");
        }

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        String hashedPin = passwordEncoder.encode(plainPin);

        Account securedAccount = account.updateTransactionPin(hashedPin);

        Account savedAccount = persistencePort.save(securedAccount);

        log.info("PIN transacional cadastrado com sucesso para a conta: {}", savedAccount.id());

        return savedAccount;
    }
}
