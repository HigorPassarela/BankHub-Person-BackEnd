package com.bankhub.account.application.port.in;

import com.bankhub.account.domain.Account;
import org.springframework.web.multipart.MultipartFile;

/**
 * Porta de entrada para o processamento de KYC (Biometria Facial).
 */
public interface UploadSelfieUseCase {

    /**
     * Recebe a selfie do cliente, salva no storage cloud e aprova a identidade.
     *
     * @param accountId ID interno da conta.
     * @param customerId ID do cliente logado (Segurança Zero Trust).
     * @param file Arquivo físico da selfie (Formato Multipart).
     * @return A conta atualizada com a flag isIdentityVerified = true.
     */
    Account execute(String accountId, String customerId, MultipartFile file);
}
