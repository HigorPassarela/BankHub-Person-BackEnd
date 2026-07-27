package com.bankhub.account.application.port.out;

import java.util.Optional;

/**
 * Porta de saída para gerenciar os Tokens Temporários (Magic Links) do banco.
 */
public interface AccountTokenPort {

    /**
     * Gera e armazena um novo token temporário vinculado a uma conta.
     *
     * @param accountId ID da conta recém-criada.
     * @return A String do token gerado (Ex: UUID).
     */
    String generateAndSaveToken(String accountId);

    /**
     * Valida um token existente e retorna o ID da conta dona dele.
     *
     * @param token O token recebido via API.
     * @return O ID da conta (se o token for válido e não expirado).
     */
    Optional<String> resolveToken(String token);

    /**
     * Exclui o token (Burn-after-use) garantindo segurança.
     *
     * @param token O token que acabou de ser utilizado com sucesso.
     */
    void revokeToken(String token);
}