package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.ResolvePixKeyUseCase;
import com.bankhub.transaction.infrastructure.client.AccountFeignClient;
import com.bankhub.transaction.infrastructure.client.dto.AccountDictResponse;
import com.bankhub.transaction.infrastructure.web.dto.PixKeyResolveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolvePixKeyService implements ResolvePixKeyUseCase {

    private final AccountFeignClient accountFeignClient;

    @Override
    public PixKeyResolveResponse execute(String pixKey, String jwtToken) {
        log.info("Consultando a chave PIX [{}] de forma síncrona no Account Service...", pixKey);

        if (pixKey == null || pixKey.isBlank()) {
            throw new IllegalArgumentException("A chave PIX não pode ser vazia.");
        }

        try {
            String bearerToken = jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken;
            AccountDictResponse response = accountFeignClient.resolveAccountByNumber(pixKey, bearerToken);

            log.info("Chave PIX resolvida! O dinheiro irá para a Conta ID: {}", response.accountId());

            return PixKeyResolveResponse.builder()
                    .pixKey(pixKey)
                    .receiverName("CLIENTE " + response.customerId().substring(0, 5).toUpperCase() + "***")
                    .maskedCpf("***.***.***-**")
                    .destinationAccountId(response.accountId())
                    .bankName("Bank-Hub S.A.")
                    .build();
        } catch (Exception e) {
            log.warn("Falha na resolução da chave PIX [{}]. Erro: {}", pixKey, e.getMessage());
            throw new IllegalArgumentException("Chave PIX inválida ou conta de destino bloqueada.");
        }
    }
}
