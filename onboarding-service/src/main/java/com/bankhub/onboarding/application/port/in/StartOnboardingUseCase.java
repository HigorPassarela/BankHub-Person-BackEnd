package com.bankhub.onboarding.application.port.in;

import java.math.BigDecimal;

/**
 * Porta de entrada (Caso de Uso) para iniciar o processo de abertura de conta.
 */
public interface StartOnboardingUseCase {

    /**
     * Inicia a esteira de aprovação do cliente.
     *
     * @param documentNumber Documento do cliente (CPF/CNPJ).
     * @param monthlyIncome  Renda mensal declarada pelo cliente.
     * @return O número de protocolo (ID da instância do processo no orquestrador).
     */
    String execute(String documentNumber, BigDecimal monthlyIncome, String fullName, String phone, String address);
}
