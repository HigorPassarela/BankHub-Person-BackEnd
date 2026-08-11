package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DepositAccountService Unit Tests")
class DepositAccountServiceTest extends BaseUnitTest {
    @Mock
    private AccountPersistencePort persistencePort;
    @InjectMocks
    private DepositAccountService depositAccountService;

    @Test
    @DisplayName("should deposit to account successfully")
    void shouldDepositSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        BigDecimal depositAmount = new BigDecimal("100.00");
        Account account = Account.builder().id(accountId).customerId(customerId).status(AccountStatus.ACTIVE).balance(new Balance(new BigDecimal("50.00"), "BRL")).build();
        Account depositedAccount = account.credit(depositAmount);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(persistencePort.save(any(Account.class))).thenReturn(depositedAccount);
        Account result = depositAccountService.execute(accountId, customerId, depositAmount);
        assertThat(result.balance().amount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
}
