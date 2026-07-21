package com.bankhub.account.infrastructure.web.controller;

import com.bankhub.account.application.port.in.ActivateAccountUseCase;
import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.application.port.in.FindAccountUseCase;
import com.bankhub.account.domain.Account;
import com.bankhub.account.infrastructure.web.api.AccountApi;
import com.bankhub.account.infrastructure.web.dto.AccountResponse;
import com.bankhub.account.infrastructure.web.mapper.AccountWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final FindAccountUseCase findAccountUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final AccountWebMapper webMapper;

    @Override
    public ResponseEntity<AccountResponse> createAccount(String customerId) {
        log.info("Recebida requisição REST para criar conta. Titular: {}", customerId);

        Account newAccount = createAccountUseCase.execute(customerId);
        AccountResponse response = webMapper.toResponse(newAccount);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AccountResponse> getAccount(String accountId, String customerId) {
        log.info("Recebida requisição REST para consultar conta. ID: {}, Titular: {}", accountId, customerId);

        Account foundAccount = findAccountUseCase.execute(accountId, customerId);
        AccountResponse response = webMapper.toResponse(foundAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> activateAccount(String accountId, String customerId) {
        log.info("Recebida requisição REST para ATIVAR a conta: {}. Titular: {}", accountId, customerId);

        Account activatedAccount = activateAccountUseCase.execute(accountId, customerId);
        AccountResponse response = webMapper.toResponse(activatedAccount);

        return ResponseEntity.ok(response);
    }
}