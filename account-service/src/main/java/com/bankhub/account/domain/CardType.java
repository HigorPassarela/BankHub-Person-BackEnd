package com.bankhub.account.domain;

/**
 * Representa a natureza física ou digital do cartão emitido para a conta.
 */
public enum CardType {
    PHYSICAL,   // Cartão plástico tradicional (Enviado pelo correio)
    VIRTUAL,    // Cartão virtual fixo para compras recorrentes (ex: Netflix, Spotify)
    TEMPORARY   // Cartão virtual de uso único (Expira em 24h)
}