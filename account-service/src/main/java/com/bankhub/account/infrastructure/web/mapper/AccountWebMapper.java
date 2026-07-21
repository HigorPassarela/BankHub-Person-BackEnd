package com.bankhub.account.infrastructure.web.mapper;

import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.AccountNumber;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.infrastructure.web.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountWebMapper {

    /**
     * Mapeia a Entidade Rica de Domínio para o DTO de saída REST.
     */
    @Mapping(source = "id", target = "account")
    @Mapping(source = "accountNumber", target = "bankDetails")
    @Mapping(source = "updatedAt", target = "lastUpdate")
    AccountResponse toResponse(Account domain);

    /**
     * Mapeia o Value Object de Saldo para o DTO aninhado.
     */
    @Mapping(source = "amount", target = "valor")
    @Mapping(source = "currency", target = "moeda")
    AccountResponse.BalanceResponse toBalanceResponse(Balance balance);

    default AccountResponse.BankDetailsResponse toBankDetailsResponse(AccountNumber number) {
        if (number == null) {
            return null;
        }
        return AccountResponse.BankDetailsResponse.builder()
                .agency(number.agency())
                .number(number.number())
                .build();
    }
}
