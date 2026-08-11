package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.InvestorProfile;
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

@DisplayName("UpdateInvestorProfileService Unit Tests")
class UpdateInvestorProfileServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private UpdateInvestorProfileService updateInvestorProfileService;

    @Test
    @DisplayName("should update investor profile successfully")
    void shouldUpdateInvestorProfileSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        InvestorProfile newProfile = InvestorProfile.MODERATE;

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .investorProfile(InvestorProfile.CONSERVATIVE)
                .balance(Balance.zero())
                .build();

        Account updatedAccount = account.updateInvestorProfile(newProfile);

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(persistencePort.save(any(Account.class))).thenReturn(updatedAccount);

        Account result = updateInvestorProfileService.execute(accountId, customerId, newProfile);

        assertThat(result.investorProfile()).isEqualTo(InvestorProfile.MODERATE);
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        InvestorProfile newProfile = InvestorProfile.MODERATE;

        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateInvestorProfileService.execute(accountId, customerId, newProfile))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Conta não encontrada");
    }
}
