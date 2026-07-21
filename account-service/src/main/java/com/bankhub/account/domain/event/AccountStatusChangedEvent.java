package com.bankhub.account.domain.event;

import com.bankhub.account.domain.Account;

/**
 * Evento de domínio disparado sempre que o status de uma conta é alterado (Ex: de PENDING para ACTIVE).
 */
public record AccountStatusChangedEvent(Account account) {
}