package com.bankhub.account.domain.event;

import com.bankhub.account.domain.Account;

/**
 * Evento de domínio disparado logo após a criação bem-sucedida de uma conta.
 * Inclui o Magic Link Token para ser despachado por e-mail.
 */
public record AccountCreatedEvent(Account account, String activationToken) {}