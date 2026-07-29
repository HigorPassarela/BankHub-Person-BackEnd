package com.bankhub.account.application.service;

import com.bankhub.account.application.port.in.UploadSelfieUseCase;
import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadSelfieService implements UploadSelfieUseCase {

    private final AccountPersistencePort persistencePort;

    @Override
    @Transactional
    public Account execute(String accountId, String customerId, MultipartFile file) {
        log.info("Iniciando processamento de KYC (Selfie Upload). Conta: {}. Tamanho do Arquivo: {} bytes",
                accountId, file.getSize());

        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo inválido. O KYC exige uma imagem no formato JPEG ou PNG.");
        }

        Account account = persistencePort.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada ou acesso negado."));

        String fakeS3Url = "https://s3.amazonaws.com/bankhub-kyc-bucket/" + accountId + "/" + UUID.randomUUID() + "-selfie.jpg";
        log.debug("Simulação de Upload concluída. URL gerada: {}", fakeS3Url);

        Account verifiedAccount = account.approveKyc(fakeS3Url);

        Account savedAccount = persistencePort.save(verifiedAccount);

        log.info("KYC aprovado com sucesso! Cliente {} está liberado para transações de risco.", customerId);

        return savedAccount;
    }
}
