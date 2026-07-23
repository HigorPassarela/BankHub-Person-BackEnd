package com.bankhub.account.application.port.out;

import com.bankhub.account.domain.Account;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para operações de persistência do agregado Account.
 */
public interface AccountPersistencePort {

    /**
     * Salva ou atualiza uma conta no repositório.
     *
     * @param account Entidade de domínio pura.
     * @return Conta salva e atualizada com dados gerados (ex: ID, Version).
     */
    Account save(Account account);

    /**
     * Busca uma conta pertencente a um cliente específico.
     *
     * @param id ID da conta.
     * @param customerId ID do dono da conta.
     * @return Optional contendo a conta, se encontrada.
     */
    Optional<Account> findByIdAndCustomerId(String id, String customerId);

    /**
     * Lista todas as contas de um cliente.
     *
     * @param customerId ID do dono da conta.
     * @return Lista de contas do domínio.
     */
    List<Account> findByCustomerId(String customerId);

    /**
     * Busca uma conta apenas pelo ID (Usado para creditar o PIX no destino).
     */
    Optional<Account> findById(String id);
}
