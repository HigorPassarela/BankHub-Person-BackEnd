package com.bankhub.account.application.service;

import com.bankhub.account.application.port.out.AccountPersistencePort;
import com.bankhub.account.base.BaseUnitTest;
import com.bankhub.account.domain.Account;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.AccountStatus;
import com.bankhub.account.domain.Balance;
import com.bankhub.account.domain.exception.AccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UploadSelfieService Unit Tests")
class UploadSelfieServiceTest extends BaseUnitTest {

    @Mock
    private AccountPersistencePort persistencePort;

    @InjectMocks
    private UploadSelfieService uploadSelfieService;

    @Test
    @DisplayName("should upload selfie successfully")
    void shouldUploadSelfieSuccessfully() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        MultipartFile file = mock(MultipartFile.class);

        Account account = Account.builder()
                .id(accountId)
                .customerId(customerId)
                .status(AccountStatus.ACTIVE)
                .balance(Balance.zero())
                .build();

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.of(account));
        when(persistencePort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = uploadSelfieService.execute(accountId, customerId, file);

        assertThat(result).isNotNull();
        assertThat(result.selfieUrl()).isNotNull();
    }

    @Test
    @DisplayName("should throw exception when file is empty")
    void shouldThrowExceptionWhenFileEmpty() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> uploadSelfieService.execute(accountId, customerId, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo inválido");
    }

    @Test
    @DisplayName("should throw exception when file is not an image")
    void shouldThrowExceptionWhenFileNotImage() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThatThrownBy(() -> uploadSelfieService.execute(accountId, customerId, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Arquivo inválido");
    }

    @Test
    @DisplayName("should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        String accountId = "acc-123";
        String customerId = "customer-001";
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(persistencePort.findByIdAndCustomerId(accountId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uploadSelfieService.execute(accountId, customerId, file))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
