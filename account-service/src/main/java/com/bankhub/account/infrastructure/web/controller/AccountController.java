package com.bankhub.account.infrastructure.web.controller;

import com.bankhub.account.application.port.in.ActivateAccountUseCase;
import com.bankhub.account.application.port.in.CreateAccountUseCase;
import com.bankhub.account.application.port.in.CreateTransactionPinUseCase;
import com.bankhub.account.application.port.in.DebitAccountUseCase;
import com.bankhub.account.application.port.in.DepositAccountUseCase;
import com.bankhub.account.application.port.in.FindAccountUseCase;
import com.bankhub.account.application.port.in.ResolveAccountDictUseCase;
import com.bankhub.account.application.port.in.UpdateInvestorProfileUseCase;
import com.bankhub.account.application.port.in.UploadSelfieUseCase;
import com.bankhub.account.application.port.in.ValidateTransactionPinUseCase;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.InvestorProfile;
import com.bankhub.account.infrastructure.web.api.AccountApi;
import com.bankhub.account.infrastructure.web.dto.AccountDictResponse;
import com.bankhub.account.infrastructure.web.dto.AccountResponse;
import com.bankhub.account.infrastructure.web.dto.DebitRequest;
import com.bankhub.account.infrastructure.web.dto.PinRequest;
import com.bankhub.account.infrastructure.web.mapper.AccountWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final CreateTransactionPinUseCase createTransactionPinUseCase;
    private final FindAccountUseCase findAccountUseCase;
    private final ActivateAccountUseCase activateAccountUseCase;
    private final DepositAccountUseCase depositAccountUseCase;
    private final DebitAccountUseCase debitAccountUseCase;
    private final UploadSelfieUseCase uploadSelfieUseCase;
    private final ResolveAccountDictUseCase resolveAccountDictUseCase;
    private final ValidateTransactionPinUseCase validateTransactionPinUseCase;
    private final UpdateInvestorProfileUseCase updateInvestorProfileUseCase;
    private final AccountWebMapper webMapper;

    @Override
    public ResponseEntity<AccountResponse> createAccount(String customerId) {
        log.info("Recebida requisição REST para criar conta. Titular: {}", customerId);

        // FIX: Adicionado placeholders. O fluxo real passa pelo OnboardingCommandListener (Kafka).
        Account newAccount = createAccountUseCase.execute(customerId, "Não Informado", "Não Informado", "Não Informado");
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
    public ResponseEntity<AccountResponse> activateAccount(@Valid @RequestBody com.bankhub.account.infrastructure.web.dto.ActivationRequest request) {
        log.info("Recebida requisição REST Pública para ATIVAR conta via Magic Link Token.");

        Account activatedAccount = activateAccountUseCase.execute(request.token());

        AccountResponse response = webMapper.toResponse(activatedAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> depositAccount(String accountId, String customerId, @Valid @RequestBody com.bankhub.account.infrastructure.web.dto.DepositRequest request) {
        log.info("Recebida requisição REST de Depósito. Conta: {}, Valor: {}", accountId, request.amount());

        Account richAccount = depositAccountUseCase.execute(accountId, customerId, request.amount());
        AccountResponse response = webMapper.toResponse(richAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> debitAccount(String accountId, String customerId, @Valid @RequestBody DebitRequest request) {
        log.info("Recebida requisição REST de Débito M2M. Conta: {}, Valor: {}", accountId, request.amount());

        Account richAccount = debitAccountUseCase.execute(accountId, customerId, request.amount());
        AccountResponse response = webMapper.toResponse(richAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> createTransactionPin(String accountId, String customerId, @Valid @RequestBody PinRequest request) {
        log.info("Recebida requisição REST para cadastrar PIN Transacional. Conta: {}", accountId);

        Account securedAccount = createTransactionPinUseCase.execute(accountId, customerId, request.transactionPin());

        AccountResponse response = webMapper.toResponse(securedAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> uploadSelfie(String accountId, String customerId, MultipartFile file) {
        log.info("Recebida requisição REST de Upload de Selfie (KYC). Conta: {}", accountId);

        Account verifiedAccount = uploadSelfieUseCase.execute(accountId, customerId, file);
        AccountResponse response = webMapper.toResponse(verifiedAccount);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountDictResponse> resolveDict(String accountNumber) {
        Account account = resolveAccountDictUseCase.execute(accountNumber);

        AccountDictResponse response = AccountDictResponse.builder()
                .accountId(account.id())
                .customerId(account.customerId())
                .agency(account.accountNumber().agency())
                .accountNumber(account.accountNumber().number())
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> validateTransaction(String accountId, String customerId, @Valid PinRequest request) {
        log.info("Recebida requisição REST para validar transação M2M (Zero Trust). Conta: {}", accountId);

        validateTransactionPinUseCase.execute(accountId, customerId, request.transactionPin());

        log.info("Validação M2M aprovada para a conta: {}", accountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AccountResponse> updateInvestorProfile(String accountId, String customerId, String profile) {
        log.info("Recebida requisição REST para atualizar Suitability (IA). Conta: {}, Perfil: {}", accountId, profile);

        Account account = updateInvestorProfileUseCase.execute(accountId, customerId, InvestorProfile.valueOf(profile.toUpperCase()));

        return ResponseEntity.ok(webMapper.toResponse(account));
    }
}