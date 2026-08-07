package com.bankhub.hubia.application.validation;

import com.bankhub.hubia.domain.ToolExecutionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Valida se ferramentas afirmadas pela IA foram realmente executadas.
 * Compara ferramentas executadas (do ToolExecutionContext) com afirmações na resposta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutionValidator {

    private final ToolExecutionContext toolExecutionContext;
    private final HallucinationDetector hallucinationDetector;

    /**
     * Resultado da validação de uma resposta da IA.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final boolean isHallucination;
        public final String hallucinatedToolName;
        public final String reason;

        private ValidationResult(boolean isValid, boolean isHallucination, String hallucinatedToolName, String reason) {
            this.isValid = isValid;
            this.isHallucination = isHallucination;
            this.hallucinatedToolName = hallucinatedToolName;
            this.reason = reason;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, false, null, "Response validated successfully");
        }

        public static ValidationResult hallucination(String toolName, String reason) {
            return new ValidationResult(false, true, toolName, reason);
        }
    }

    /**
     * Valida se a resposta da IA está correta em relação às ferramentas executadas.
     *
     * @param aiResponse A resposta gerada pela IA
     * @return Resultado da validação
     */
    public ValidationResult validateResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return ValidationResult.valid();
        }

        // Verifica se a resposta afirma ter atualizado perfil, mas updateInvestorProfileTool não foi executada
        if (hallucinationDetector.detectsActionOnConcept(aiResponse, "perfil")) {
            boolean toolWasExecuted = toolExecutionContext.wasExecuted("updateInvestorProfileTool");

            if (!toolWasExecuted) {
                String reason = "AI afirma ter atualizado perfil, mas updateInvestorProfileTool não foi executada";
                log.warn("[Validation] ALUCINAÇÃO DETECTADA: {}", reason);

                // Registra evento de alucinação
                ToolExecutionEvent hallucinationEvent = ToolExecutionEvent.hallucination(
                        "updateInvestorProfileTool",
                        truncate(aiResponse, 200),
                        toolExecutionContext.getAccountId(),
                        toolExecutionContext.getConversationId()
                );
                toolExecutionContext.addAuditEvent(hallucinationEvent);

                return ValidationResult.hallucination("updateInvestorProfileTool", reason);
            }
        }

        // Verifica outras alucinações genéricas
        if (hallucinationDetector.detectsToolClaim(aiResponse, null)) {
            // Aqui poderíamos adicionar mais verificações específicas
            // Por ora, se passou pelas verificações anteriores e detectamos afirmação de ação,
            // assumimos que é válido (pode ser uma ação que não requer ferramenta)
            log.debug("[Validation] Resposta contém afirmação de ação, mas não detectamos alucinação específica");
        }

        log.info("[Validation] Resposta validada com sucesso");
        return ValidationResult.valid();
    }

    /**
     * Trunca uma string para um tamanho máximo.
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Gera uma mensagem de fallback amigável quando uma alucinação é detectada.
     *
     * @param hallucinatedToolName Nome da ferramenta que deveria ter sido executada
     * @return Mensagem de fallback para o usuário
     */
    public String generateFallbackMessage(String hallucinatedToolName) {
        log.info("[Validation] Gerando mensagem de fallback para ferramenta alucinada: {}", hallucinatedToolName);

        // Mensagem genérica user-friendly que não revela detalhes técnicos
        return "Desculpe, encontrei um problema ao completar essa ação. " +
                "Por favor, tente novamente ou entre em contato com o suporte se o problema persistir.";
    }
}
