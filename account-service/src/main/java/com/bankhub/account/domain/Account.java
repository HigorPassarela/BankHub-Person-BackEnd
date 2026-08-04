package com.bankhub.account.domain;

import com.bankhub.account.domain.exception.InsufficientFundsException;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record Account(
        String id,
        String customerId,
        String fullName,
        String phone,
        String address,
        AccountNumber accountNumber,
        Balance balance,
        AccountStatus status,
        Long version,
        String transactionPinHash,
        boolean isIdentityVerified,
        String selfieUrl,
        InvestorProfile investorProfile,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public Account activate() {
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(balance).status(AccountStatus.ACTIVE).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account block() {
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(balance).status(AccountStatus.BLOCKED).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de crédito deve ser maior que zero.");
        }
        Balance newBalance = new Balance(this.balance.amount().add(amount), this.balance.currency());
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(newBalance).status(status).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de débito deve ser maior que zero.");
        }
        if (this.balance.amount().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente para realizar esta operação.");
        }
        Balance newBalance = new Balance(this.balance.amount().subtract(amount), this.balance.currency());
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(newBalance).status(status).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account approveKyc(String savedSelfieUrl) {
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(balance).status(status).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(true).selfieUrl(savedSelfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account updateTransactionPin(String newHashedPin) {
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(balance).status(status).version(version)
                .transactionPinHash(newHashedPin).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(investorProfile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }

    public Account updateInvestorProfile(InvestorProfile profile) {
        return Account.builder().id(id).customerId(customerId).fullName(fullName).phone(phone).address(address)
                .accountNumber(accountNumber).balance(balance).status(status).version(version)
                .transactionPinHash(transactionPinHash).isIdentityVerified(isIdentityVerified).selfieUrl(selfieUrl)
                .investorProfile(profile).createdAt(createdAt).updatedAt(LocalDateTime.now()).build();
    }
}