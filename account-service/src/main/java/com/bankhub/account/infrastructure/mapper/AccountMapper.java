package com.bankhub.account.infrastructure.mapper;

import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountNumber;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.infrastructure.persistence.entity.AccountDocument;
import com.bankhub.account.infrastructure.persistence.entity.AccountNumberModel;
import com.bankhub.account.infrastructure.persistence.entity.BalanceModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toDomain(AccountDocument document);

    AccountDocument toDocument(Account domain);

    default Balance toDomainBalance(BalanceModel model) {
        if (model == null || model.getAmount() == null) {
            return Balance.zero();
        }
        return new Balance(model.getAmount(), model.getCurrency());
    }

    default BalanceModel toModelBalance(Balance domain) {
        if (domain == null || domain.amount() == null) {
            return null;
        }
        return BalanceModel.builder()
                .amount(domain.amount())
                .currency(domain.currency())
                .build();
    }

    default AccountNumber toDomainAccountNumber(AccountNumberModel model) {
        if (model == null || model.getAgency() == null || model.getNumber() == null) {
            return null;
        }
        return new AccountNumber(model.getAgency(), model.getNumber());
    }

    default AccountNumberModel toModelAccountNumber(AccountNumber domain) {
        if (domain == null) {
            return null;
        }
        return AccountNumberModel.builder()
                .agency(domain.agency())
                .number(domain.number())
                .build();
    }
}