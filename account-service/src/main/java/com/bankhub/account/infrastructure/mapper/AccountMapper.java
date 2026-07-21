package com.bankhub.account.infrastructure.mapper;

import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.infrastructure.persistence.entity.AccountDocument;
import com.bankhub.account.infrastructure.persistence.entity.BalanceModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Converte o documento do MongoDB para a Entidade Rica de Domínio.
     */
    Account toDomain(AccountDocument document);

    /**
     * Converte a Entidade de Domínio para o documento físico do MongoDB.
     */
    AccountDocument toDocument(Account domain);

    /**
     * Converte o sub-documento de saldo para o Value Object de Domínio.
     * MUDANÇA: Usa um método 'default' para blindar contra saldos nulos vindos do banco!
     */
    default Balance toDomainBalance(BalanceModel model) {
        if (model == null || model.getAmounts() == null) {
            return Balance.zero();
        }
        return new Balance(model.getAmounts(), model.getCurrency());
    }
    /**
     * Converte o Value Object de Domínio para o sub-documento do MongoDB.
     */
    BalanceModel toModelBalance(Balance domain);

}
