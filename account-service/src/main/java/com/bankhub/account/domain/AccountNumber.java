package com.bankhub.account.domain;

/**
 * Value Object que representa os dados bancários amigáveis (Agência e Conta) para o cliente.
 */
public record AccountNumber(String agency, String number) {

    public AccountNumber {
        if (agency == null || agency.isBlank()) {
            throw new IllegalArgumentException("A Agência não pode ser nula ou vazia.");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("O Número da Conta não pode ser nulo ou vazio.");
        }

        agency = agency.trim();
        number = number.trim();
    }

    /**
     * Factory Method que formata a conta amigavelmente para exibição e login.
     * Exemplo de saída: "0001 / 123456-7"
     */
    public String getFormatted() {
        return agency + " / " + number;
    }
}