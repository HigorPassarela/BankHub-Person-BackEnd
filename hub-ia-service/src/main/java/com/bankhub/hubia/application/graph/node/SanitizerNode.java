package com.bankhub.hubia.application.graph.node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class SanitizerNode {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\b\\d{5,8}-?\\d{1}\\b");

    /**
     * Executa a higienização do texto.
     *
     * @param rawInput Texto bruto inserido pelo usuário.
     * @return Texto sanitizado.
     */
    public String execute(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return rawInput;
        }

        String sanitized = CPF_PATTERN.matcher(rawInput).replaceAll("[CPF_MASCARADO]");
        sanitized = ACCOUNT_PATTERN.matcher(sanitized).replaceAll("[CONTA_MASCARADA]");

        if (!rawInput.equals(sanitized)) {
            log.info("SanitizerNode: Dados sensíveis interceptados e mascarados com sucesso.");
        }

        return sanitized;
    }
}
