package com.bankhub.transaction.application.port.in;

import com.bankhub.transaction.infrastructure.web.dto.PixKeyResolveResponse;

/**
 * Porta de entrada para a consulta no DICT (Diretório do Banco Central).
 */
public interface ResolvePixKeyUseCase {

    /**
     * Valida uma chave PIX e retorna os dados do recebedor.
     *
     * @param pixKey A chave (CPF, E-mail, Celular ou Aleatória).
     * @return DTO com o nome e as coordenadas bancárias.
     */
    PixKeyResolveResponse execute(String pixKey);
}
