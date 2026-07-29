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

    default Account toDomain(AccountDocument document) {
        if (document == null) {
            return null;
        }

        return Account.builder()
                .id(document.getId())
                .customerId(document.getCustomerId())
                .accountNumber(toDomainAccountNumber(document.getAccountNumber()))
                .balance(toDomainBalance(document.getBalance()))
                .status(document.getStatus())
                .transactionPinHash(document.getTransactionPinHash())
                .isIdentityVerified(document.isIdentityVerified())
                .selfieUrl(document.getSelfieUrl())
                .version(document.getVersion())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    default AccountDocument toDocument(Account domain) {
        if (domain == null) {
            return null;
        }

        return AccountDocument.builder()
                .id(domain.id())
                .customerId(domain.customerId())
                .accountNumber(toModelAccountNumber(domain.accountNumber()))
                .balance(toModelBalance(domain.balance()))
                .status(domain.status())
                .transactionPinHash(domain.transactionPinHash())
                .isIdentityVerified(domain.isIdentityVerified())
                .selfieUrl(domain.selfieUrl())
                .version(domain.version())
                .createdAt(domain.createdAt())
                .updatedAt(domain.updatedAt())
                .build();
    }

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