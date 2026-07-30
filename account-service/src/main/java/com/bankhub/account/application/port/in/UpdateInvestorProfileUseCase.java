package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.InvestorProfile;

public interface UpdateInvestorProfileUseCase {
    Account execute(String accountId, String customerId, InvestorProfile  profile);
}
