package com.bankhub.transaction.application.service;

import com.bankhub.transaction.base.BaseUnitTest;
import com.bankhub.transaction.infrastructure.web.dto.BoletoResolveResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ResolveBoletoService Unit Tests")
class ResolveBoletoServiceTest extends BaseUnitTest {

    @InjectMocks
    private ResolveBoletoService resolveBoletoService;

    @Test
    @DisplayName("should resolve boleto successfully for energy company")
    void shouldResolveBoletoForEnergyCompany() {
        // Arrange
        String barcode = "84600000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
        assertThat(response.companyName()).isEqualTo("Companhia de Energia Elétrica (Light/Enel)");
        assertThat(response.amount()).isNotNull();
        assertThat(response.dueDate()).isAfter(LocalDate.now());
        assertThat(response.isExpired()).isFalse();
    }

    @Test
    @DisplayName("should resolve boleto successfully for sanitation company")
    void shouldResolveBoletoForSanitationCompany() {
        // Arrange
        String barcode = "84800000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
        assertThat(response.companyName()).isEqualTo("Companhia de Saneamento Básico (Sabesp/Cedae)");
        assertThat(response.dueDate()).isAfter(LocalDate.now());
        assertThat(response.isExpired()).isFalse();
    }

    @Test
    @DisplayName("should resolve boleto successfully for Bradesco bank")
    void shouldResolveBoletoForBradesco() {
        // Arrange
        String barcode = "23700000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
        assertThat(response.companyName()).isEqualTo("Banco Bradesco S.A.");
    }

    @Test
    @DisplayName("should resolve boleto successfully for Itaú bank")
    void shouldResolveBoletoForItau() {
        // Arrange
        String barcode = "34100000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
        assertThat(response.companyName()).isEqualTo("Banco Itaú Unibanco S.A.");
    }

    @Test
    @DisplayName("should resolve boleto with default company name for unknown prefix")
    void shouldResolveBoletoWithDefaultCompanyName() {
        // Arrange
        String barcode = "99900000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
        assertThat(response.companyName()).isEqualTo("Pagamento Diversos / E-commerce (MercadoPago)");
    }

    @Test
    @DisplayName("should mark boleto as expired when barcode starts with 88")
    void shouldMarkBoletoAsExpiredWhenStartsWith88() {
        // Arrange
        String barcode = "88800000000012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isExpired()).isTrue();
        assertThat(response.dueDate()).isBefore(LocalDate.now());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when barcode is null")
    void shouldThrowExceptionWhenBarcodeIsNull() {
        // Act & Assert
        assertThatThrownBy(() ->
                resolveBoletoService.execute(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de barras inválido");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when barcode has invalid format (too short)")
    void shouldThrowExceptionWhenBarcodeTooShort() {
        // Arrange
        String barcode = "123456789"; // Only 9 digits

        // Act & Assert
        assertThatThrownBy(() ->
                resolveBoletoService.execute(barcode)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de barras inválido");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when barcode contains non-numeric characters")
    void shouldThrowExceptionWhenBarcodeContainsLetters() {
        // Arrange
        String barcode = "123ABC7890123456789012345678";

        // Act & Assert
        assertThatThrownBy(() ->
                resolveBoletoService.execute(barcode)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de barras inválido");
    }

    @Test
    @DisplayName("should accept minimum valid barcode length (10 digits)")
    void shouldAcceptMinimumValidBarcodeLength() {
        // Arrange
        String barcode = "1234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
    }

    @Test
    @DisplayName("should accept maximum valid barcode length (50 digits)")
    void shouldAcceptMaximumValidBarcodeLength() {
        // Arrange
        String barcode = "12345678901234567890123456789012345678901234567890";

        // Act
        BoletoResolveResponse response = resolveBoletoService.execute(barcode);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.barcode()).isEqualTo(barcode);
    }
}
