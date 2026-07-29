package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.ValidateTransactionPinUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateTransactionPinService implements ValidateTransactionPinUseCase {

    private final AccountPersistencePort persistencePort;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean execute(String accountId, String customerId, String plainPin) {
        log.info("Iniciando validação de PIN transacional para a conta: {}. Solicitante: {}", accountId, customerId);

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        if (account.transactionPinHash() == null || account.transactionPinHash().isBlank()) {
            log.error("Tentativa de transação bloqueada. Conta {} não possui PIN cadastrado.", accountId);
            throw new SecurityException("Transação negada. Nenhum PIN de segurança cadastrado para esta conta.");
        }

        boolean isMatch = passwordEncoder.matches(plainPin, account.transactionPinHash());

        if (!isMatch) {
            log.warn("Alerta de Segurança: Tentativa de transação com PIN INVÁLIDO. Conta: {}", accountId);
            throw new SecurityException("A assinatura eletrônica (PIN) fornecida está incorreta.");
        }

        log.info("Validação de segurança concluída com sucesso. PIN aprovado para a conta: {}", accountId);
        return true;
    }
}
