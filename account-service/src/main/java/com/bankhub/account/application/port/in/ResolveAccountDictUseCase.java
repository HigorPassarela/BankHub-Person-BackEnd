package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

public interface ResolveAccountDictUseCase {
    Account execute(String accountNumber);
}