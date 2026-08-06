package com.bankhub.investment.application.service;

import com.bankhub.investment.application.port.out.AccountDebitPort;
import com.bankhub.investment.application.port.out.PortfolioPersistencePort;
import com.bankhub.investment.domain.Portfolio;
import com.bankhub.investment.infrastructure.client.AccountFeignClient;
import com.bankhub.investment.infrastructure.client.TransactionFeignClient;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyAssetServiceTest {

    @Mock
    private PortfolioPersistencePort persistencePort;
    @Mock
    private AccountDebitPort accountDebitPort;
    @Mock
    private AccountFeignClient accountFeignClient;
    @Mock
    private TransactionFeignClient transactionFeignClient;

    @InjectMocks
    private BuyAssetService buyAssetService;

    @Test
    @DisplayName("Deve comprar ativo e adicionar ao portfolio com sucesso")
    void shouldBuyAssetSuccessfully() {
        // Arrange
        String customerId = "customer-123";
        String accountId = "acc-123";
        
        // Simula que a validação do PIN passou (não lança exceção)
        doNothing().when(accountFeignClient).validateTransaction(any(), any(), any());
        
        // Simula o débito com sucesso
        doNothing().when(accountDebitPort).debitFunds(any(), any(), any());
        
        // Simula carteira vazia
        when(persistencePort.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(persistencePort.save(any(Portfolio.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Portfolio result = buyAssetService.execute(customerId, accountId, "PETR4", "STOCK", new BigDecimal("10"), "1234");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.assets().size());
        assertEquals("PETR4", result.assets().get(0).ticker());
        
        verify(accountDebitPort, times(1)).debitFunds(eq(accountId), eq(customerId), any(BigDecimal.class));
        verify(transactionFeignClient, times(1)).registerLedger(anyMap());
    }

    @Test
    @DisplayName("Deve bloquear compra se o PIN for inválido")
    void shouldBlockPurchaseWhenPinIsInvalid() {
        // Arrange
        FeignException.Forbidden forbiddenMock = mock(FeignException.Forbidden.class);
        doThrow(forbiddenMock).when(accountFeignClient).validateTransaction(any(), any(), any());

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> 
            buyAssetService.execute("cust-1", "acc-1", "PETR4", "STOCK", BigDecimal.TEN, "0000")
        );
        
        assertTrue(exception.getMessage().contains("Transação negada"));
        verify(accountDebitPort, never()).debitFunds(any(), any(), any()); // Garante que não debitou dinheiro!
    }
}
