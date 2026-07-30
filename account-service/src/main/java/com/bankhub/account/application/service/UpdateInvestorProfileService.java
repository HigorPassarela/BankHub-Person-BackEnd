package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.UpdateInvestorProfileUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.InvestorProfile;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateInvestorProfileService implements UpdateInvestorProfileUseCase {

    private final AccountPersistencePort persistencePort;

    @Override
    public Account execute(String accountId, String customerId, InvestorProfile profile) {
        log.info("Atualizando Perfil de Investidor da conta {} para [{}]", accountId, profile);

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada."));

        Account updatedAccount = account.updateInvestorProfile(profile);

        return persistencePort.save(updatedAccount);
    }
}
