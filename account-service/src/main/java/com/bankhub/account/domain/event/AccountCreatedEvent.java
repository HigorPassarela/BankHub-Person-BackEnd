package com.bankhub.account.domain.event;

import com.bankhub.account.domain.Account;

/**
 * Evento de domínio disparado logo após a criação bem-sucedida de uma conta.
 *
 * @param account O snapshot da conta recém-criada.
 */
public record AccountCreatedEvent(Account account) {
}
