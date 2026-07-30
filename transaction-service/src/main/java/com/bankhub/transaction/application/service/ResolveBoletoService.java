package com.bankhub.transaction.application.service;

import com.bankhub.transaction.application.port.in.ResolveBoletoUseCase;
import com.bankhub.transaction.infrastructure.web.dto.BoletoResolveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Service
public class ResolveBoletoService implements ResolveBoletoUseCase {

    @Override
    public BoletoResolveResponse execute(String barcode) {
        log.info("Consultando a linha digitável do boleto [{}] na Câmara de Compensação...", barcode);

        if (barcode == null || !barcode.matches("^\\d{10,50}$")) {
            log.warn("Tentativa de consulta de boleto bloqueada. Formato inválido.");
            throw new IllegalArgumentException("Código de barras inválido. Deve conter apenas números (mínimo de 10 dígitos).");
        }

        String company = resolveCompanyName(barcode);
        BigDecimal amount = new BigDecimal(new Random().nextInt(500) + 50 + ".00");

        boolean simulatedExpired = barcode.startsWith("88");
        LocalDate dueDate = simulatedExpired ? LocalDate.now().minusDays(5) : LocalDate.now().plusDays(10);

        log.info("Boleto resolvido: Beneficiário [{}], Valor [R$ {}], Vencimento [{}]", company, amount, dueDate);

        return BoletoResolveResponse.builder()
                .barcode(barcode)
                .companyName(company)
                .amount(amount)
                .dueDate(dueDate)
                .isExpired(simulatedExpired)
                .build();
    }

    private String resolveCompanyName(String barcode) {
        if (barcode.startsWith("846")) {
            return "Companhia de Energia Elétrica (Light/Enel)";
        } else if (barcode.startsWith("848")) {
            return "Companhia de Saneamento Básico (Sabesp/Cedae)";
        } else if (barcode.startsWith("237")) {
            return "Banco Bradesco S.A.";
        } else if (barcode.startsWith("341")) {
            return "Banco Itaú Unibanco S.A.";
        }
        return "Pagamento Diversos / E-commerce (MercadoPago)";
    }
}
