package com.bankhub.hubia.application.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detecta alucinações de execução de ferramentas em respostas da IA.
 * Usa matching de keywords e correlação com nomes de ferramentas para identificar
 * quando a IA afirma ter executado uma ação sem ter efetivamente chamado a ferramenta.
 */
@Slf4j
@Service
public class HallucinationDetector {

    /**
     * Keywords que indicam que a IA afirma ter executado uma ação.
     */
    private static final Set<String> ACTION_KEYWORDS = Set.of(
            "atualizado",
            "atualizei",
            "salvei",
            "registrei",
            "configurei",
            "modifiquei",
            "alterei",
            "cadastrei",
            "criei",
            "removi",
            "deletei"
    );

    /**
     * Frases que são seguras mesmo que contenham keywords de ação.
     * Estas NÃO indicam execução de ferramenta.
     */
    private static final Set<String> SAFE_PHRASES = Set.of(
            "vou atualizar",
            "posso atualizar",
            "irei atualizar",
            "poderia atualizar",
            "gostaria de atualizar",
            "vou salvar",
            "posso salvar",
            "preciso atualizar",
            "é necessário atualizar",
            "deveria atualizar"
    );

    /**
     * Nomes de ferramentas conhecidas (para correlação).
     */
    private static final Set<String> KNOWN_TOOL_NAMES = Set.of(
            "updateInvestorProfileTool",
            "getAccountData"
    );

    /**
     * Detecta se a resposta da IA contém uma afirmação de ter executado uma ferramenta.
     *
     * @param response A resposta gerada pela IA
     * @param toolName Nome da ferramenta que deveria ter sido executada (opcional)
     * @return true se detecta uma possível alucinação
     */
    public boolean detectsToolClaim(String response, String toolName) {
        if (response == null || response.isBlank()) {
            return false;
        }

        String responseLower = response.toLowerCase();

        // Verifica se contém frases seguras (futuro/condicional) - se sim, não é alucinação
        for (String safePhrase : SAFE_PHRASES) {
            if (responseLower.contains(safePhrase)) {
                log.debug("[Hallucination Detector] Resposta contém frase segura: '{}'", safePhrase);
                return false;
            }
        }

        // Verifica se contém keywords de ação
        boolean containsActionKeyword = false;
        for (String keyword : ACTION_KEYWORDS) {
            if (responseLower.contains(keyword)) {
                containsActionKeyword = true;
                log.debug("[Hallucination Detector] Resposta contém keyword de ação: '{}'", keyword);
                break;
            }
        }

        if (!containsActionKeyword) {
            return false; // Não contém nenhuma afirmação de ação
        }

        // Se foi fornecido um nome de ferramenta específico, verifica correlação
        if (toolName != null && !toolName.isBlank()) {
            // Verifica se a resposta menciona conceitos relacionados à ferramenta
            boolean hasToolCorrelation = checkToolCorrelation(responseLower, toolName);

            if (hasToolCorrelation) {
                log.info("[Hallucination Detector] Detectada possível alucinação: ferramenta '{}' não executada mas resposta afirma ação", toolName);
                return true;
            }
        }

        // Se contém keyword de ação mas não podemos correlacionar com ferramenta específica,
        // assume que é uma afirmação de ação (possível alucinação)
        log.info("[Hallucination Detector] Detectada possível alucinação: resposta afirma ação mas sem ferramenta correlacionada");
        return containsActionKeyword;
    }

    /**
     * Verifica se a resposta menciona conceitos relacionados à ferramenta.
     */
    private boolean checkToolCorrelation(String responseLower, String toolName) {
        // Correlação baseada no nome da ferramenta
        if (toolName.toLowerCase().contains("profile")) {
            return responseLower.contains("perfil") || responseLower.contains("suitability");
        }

        if (toolName.toLowerCase().contains("account")) {
            return responseLower.contains("conta") || responseLower.contains("saldo");
        }

        // Fallback: se não conseguimos correlacionar especificamente, assume correlação
        return true;
    }

    /**
     * Detecta se a resposta contém afirmação de ação sobre um conceito específico.
     *
     * @param response A resposta da IA
     * @param concept  O conceito (ex: "perfil", "conta", "saldo")
     * @return true se detecta afirmação de ação sobre o conceito
     */
    public boolean detectsActionOnConcept(String response, String concept) {
        if (response == null || response.isBlank() || concept == null || concept.isBlank()) {
            return false;
        }

        String responseLower = response.toLowerCase();
        String conceptLower = concept.toLowerCase();

        // Verifica se menciona o conceito
        if (!responseLower.contains(conceptLower)) {
            return false;
        }

        // Verifica se contém keyword de ação
        for (String keyword : ACTION_KEYWORDS) {
            if (responseLower.contains(keyword)) {
                log.info("[Hallucination Detector] Detectada afirmação de ação sobre conceito '{}': keyword '{}'", concept, keyword);
                return true;
            }
        }

        return false;
    }
}
