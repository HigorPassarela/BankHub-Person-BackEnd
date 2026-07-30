package com.bankhub.transaction.application.port.in;

import com.bankhub.transaction.infrastructure.web.dto.BoletoResolveResponse;

/**
 * Porta de entrada para a consulta e validação na Câmara de Compensação (Boletos).
 */
public interface ResolveBoletoUseCase {

    /**
     * Valida um código de barras e retorna os dados de cobrança.
     *
     * @param barcode A linha digitável do boleto (mínimo de 10 dígitos).
     * @return DTO com os metadados da fatura.
     */
    BoletoResolveResponse execute(String barcode);

}