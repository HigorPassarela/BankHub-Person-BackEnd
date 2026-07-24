package com.bankhub.account.domain.exception;

/**
 * Exceção disparada quando uma operação de débito ultrapassa o limite permitido da conta.
 */
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
