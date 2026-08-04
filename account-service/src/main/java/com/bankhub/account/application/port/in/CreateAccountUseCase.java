package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;

/**
 * Porta de entrada (Caso de Uso) para a criação de uma nova conta bancária.
 */
public interface CreateAccountUseCase {

    Account execute(String customerId, String fullName, String phone, String address);
    
}
