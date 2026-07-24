package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

import java.math.BigDecimal;

public interface DebitAccountUseCase {
    Account execute(String accountId, String customerId, BigDecimal amount);
}
