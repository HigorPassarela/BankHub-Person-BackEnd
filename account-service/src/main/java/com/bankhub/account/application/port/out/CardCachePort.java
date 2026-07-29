package com.bankhub.account.application.port.out;

/**
 * Porta de saída para gerenciar o ciclo de vida efêmero (TTL) dos cartões virtuais temporários.
 */
public interface CardCachePort {

    /**
     * Grava o ID de um cartão temporário na memória com um prazo de validade rígido.
     *
     * @param cardId O ID do cartão salvo no MongoDB.
     */
    void registerTemporaryCard(String cardId);

    /**
     * Valida se um cartão temporário ainda está dentro do prazo de 24 horas.
     *
     * @param cardId O ID do cartão.
     * @return TRUE se o cartão existir na memória e não estiver expirado.
     */
    boolean isTemporaryCardValid(String cardId);

    /**
     * Força a exclusão do cartão da memória (ex: caso o cliente delete o cartão manualmente antes de expirar).
     */
    void removeTemporaryCard(String cardId);
}
