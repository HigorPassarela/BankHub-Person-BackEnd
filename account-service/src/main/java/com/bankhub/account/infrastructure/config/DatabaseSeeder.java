package com.bankhub.account.infrastructure.config;

import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.InvestorProfile;
import com.bankhub.account.infrastructure.persistence.entity.AccountDocument;
import com.bankhub.account.infrastructure.persistence.entity.AccountNumberModel;
import com.bankhub.account.infrastructure.persistence.entity.BalanceModel;
import com.bankhub.account.infrastructure.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) {
        log.info("Verificando se as contas de teste (QA) precisam ser injetadas no MongoDB...");

        injectAccount1();
        injectAccount2();

        log.info("Verificação de Seeder concluída.");
    }

    private void injectAccount1() {
        String accountId = "6a735aee794c397265910367";
        String customerId = "0001-C7C846-0";

        if (!accountRepository.existsById(accountId)) {
            AccountDocument account1 = AccountDocument.builder()
                    .id(accountId)
                    .customerId("0001-C7C846-0")
                    .fullName("Cliente Arquitetura Limpa")
                    .phone("11999999999")
                    .address("Avenida do Gateway, 8080")
                    .accountNumber(AccountNumberModel.builder().agency("0001").number("C7C846-0").build())
                    .balance(BalanceModel.builder().amount(new BigDecimal("5000.00")).currency("BRL").build())
                    .status(AccountStatus.ACTIVE)
                    .transactionPinHash("$2a$10$D39hMRx7F0EEvGVpUThKoebvH6hDkpfuv.KAIQaugGuGqxr2uYtc6") // PIN: 1234
                    .isIdentityVerified(false)
                    .investorProfile(InvestorProfile.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            accountRepository.save(account1);

            redisTemplate.opsForValue().set("status:account:" + accountId, "ACTIVE");

            log.info("Conta de Teste 1 (Arquitetura Limpa) injetada com sucesso!");
            createKeycloakUser(customerId, "123456");
        }
    }

    private void injectAccount2() {
        String accountId = "6a73605ec3f013305b97f7c3";
        String customerId = "0001-0F5201-3";

        if (!accountRepository.existsById(accountId)) {
            AccountDocument account2 = AccountDocument.builder()
                    .id(accountId)
                    .customerId("0001-0F5201-3")
                    .fullName("Cliente Frontend Final")
                    .phone("11999999999")
                    .address("Endereço Pendente de Cadastro")
                    .accountNumber(AccountNumberModel.builder().agency("0001").number("0F5201-3").build())
                    .balance(BalanceModel.builder().amount(new BigDecimal("9716.00")).currency("BRL").build())
                    .status(AccountStatus.ACTIVE)
                    .transactionPinHash("$2a$10$sk0gUvQeaN91.XyU9qTNDOAaThaPHHu1.9HiRopCaPFghjknne93i") // PIN: 1234
                    .isIdentityVerified(true)
                    .selfieUrl("https://s3.amazonaws.com/bankhub-kyc-bucket/6a73605ec3f013305b97f7c3/b42eb621-5603-4aae-aa62-5e625eba3d8c-selfie.jpg")
                    .investorProfile(InvestorProfile.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            accountRepository.save(account2);

            redisTemplate.opsForValue().set("status:account:" + accountId, "ACTIVE");

            log.info("Conta de Teste 2 (Frontend Final) injetada com sucesso!");
            createKeycloakUser(customerId, "123456");
        }
    }

    private void createKeycloakUser(String username, String password) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "admin-cli");
            body.add("username", "admin");
            body.add("password", "admin");

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "http://bank-keycloak:8080/realms/master/protocol/openid-connect/token",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            String token = (String) tokenResponse.getBody().get("access_token");

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setContentType(MediaType.APPLICATION_JSON);
            userHeaders.setBearerAuth(token);

            Map<String, Object> userRequest = Map.of(
                    "username", username.toLowerCase(),
                    "enabled", true,
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", password,
                            "temporary", false
                    ))
            );

            restTemplate.postForEntity(
                    "http://bank-keycloak:8080/admin/realms/master/users",
                    new HttpEntity<>(userRequest, userHeaders),
                    String.class
            );

            log.info("🔐 Usuário {} injetado com sucesso no Keycloak com a senha '{}'!", username, password);
        } catch (Exception e) {
            log.warn("Aviso: Não foi possível injetar o usuário {} no Keycloak (Ele pode já existir).", username);
        }
    }
}
