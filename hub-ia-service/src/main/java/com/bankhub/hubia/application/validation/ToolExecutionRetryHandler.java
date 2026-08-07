package com.bankhub.hubia.application.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Lida com tentativas de reexecução de ferramentas quando falham.
 * Implementa retry logic com backoff exponencial para recuperação de falhas temporárias.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutionRetryHandler {

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final ToolExecutionContext toolExecutionContext;

    /**
     * Resultado de uma tentativa de retry.
     */
    public static class RetryResult<T> {
        public final boolean success;
        public final T result;
        public final int attemptsMade;
        public final Throwable lastError;

        private RetryResult(boolean success, T result, int attemptsMade, Throwable lastError) {
            this.success = success;
            this.result = result;
            this.attemptsMade = attemptsMade;
            this.lastError = lastError;
        }

        public static <T> RetryResult<T> success(T result, int attempts) {
            return new RetryResult<>(true, result, attempts, null);
        }

        public static <T> RetryResult<T> failed(int attempts, Throwable error) {
            return new RetryResult<>(false, null, attempts, error);
        }
    }

    /**
     * Executa uma operação com retry automático em caso de falha.
     *
     * @param toolName    Nome da ferramenta sendo executada
     * @param operation   A operação a ser executada
     * @param <T>         Tipo do resultado
     * @return Resultado da tentativa de execução
     */
    public <T> RetryResult<T> executeWithRetry(String toolName, Supplier<T> operation) {
        log.info("[Retry Handler] Iniciando execução com retry para ferramenta: {}", toolName);

        Throwable lastException = null;
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("[Retry Handler] Tentativa {}/{} para ferramenta: {}", attempt, MAX_RETRIES, toolName);

                // Executa a operação
                T result = operation.get();

                if (attempt > 1) {
                    log.info("[Retry Handler] Ferramenta {} executada com sucesso na tentativa {}", toolName, attempt);
                }

                return RetryResult.success(result, attempt);

            } catch (Exception e) {
                lastException = e;
                log.warn("[Retry Handler] Tentativa {}/{} falhou para ferramenta {}: {}",
                        attempt, MAX_RETRIES, toolName, e.getMessage());

                // Se não é a última tentativa, aguarda antes de tentar novamente
                if (attempt < MAX_RETRIES) {
                    try {
                        log.debug("[Retry Handler] Aguardando {}ms antes da próxima tentativa", backoffMs);
                        Thread.sleep(backoffMs);
                        backoffMs = (long) (backoffMs * BACKOFF_MULTIPLIER);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[Retry Handler] Thread interrompida durante backoff");
                        break;
                    }
                }
            }
        }

        log.error("[Retry Handler] Ferramenta {} falhou após {} tentativas: {}",
                toolName, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown error");

        return RetryResult.failed(MAX_RETRIES, lastException);
    }

    /**
     * Verifica se um tipo de erro é retryable (pode ser tentado novamente).
     *
     * @param error O erro ocorrido
     * @return true se o erro é temporário e vale a pena tentar novamente
     */
    public boolean isRetryableError(Throwable error) {
        if (error == null) {
            return false;
        }

        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            return false;
        }

        // Erros de timeout, conexão, indisponibilidade temporária são retryable
        String lowerMessage = errorMessage.toLowerCase();
        return lowerMessage.contains("timeout") ||
                lowerMessage.contains("connection") ||
                lowerMessage.contains("unavailable") ||
                lowerMessage.contains("temporarily") ||
                lowerMessage.contains("retry");
    }

    /**
     * Executa uma operação com retry condicional baseado no tipo de erro.
     *
     * @param toolName    Nome da ferramenta sendo executada
     * @param operation   A operação a ser executada
     * @param <T>         Tipo do resultado
     * @return Resultado da tentativa de execução
     */
    public <T> RetryResult<T> executeWithConditionalRetry(String toolName, Supplier<T> operation) {
        log.info("[Retry Handler] Iniciando execução com retry condicional para ferramenta: {}", toolName);

        Throwable lastException = null;
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("[Retry Handler] Tentativa {}/{} para ferramenta: {}", attempt, MAX_RETRIES, toolName);

                T result = operation.get();

                if (attempt > 1) {
                    log.info("[Retry Handler] Ferramenta {} executada com sucesso na tentativa {}", toolName, attempt);
                }

                return RetryResult.success(result, attempt);

            } catch (Exception e) {
                lastException = e;
                log.warn("[Retry Handler] Tentativa {}/{} falhou para ferramenta {}: {}",
                        attempt, MAX_RETRIES, toolName, e.getMessage());

                // Verifica se o erro é retryable
                if (!isRetryableError(e)) {
                    log.info("[Retry Handler] Erro não é retryable, abortando retry para ferramenta {}", toolName);
                    return RetryResult.failed(attempt, e);
                }

                // Se não é a última tentativa, aguarda antes de tentar novamente
                if (attempt < MAX_RETRIES) {
                    try {
                        log.debug("[Retry Handler] Aguardando {}ms antes da próxima tentativa", backoffMs);
                        Thread.sleep(backoffMs);
                        backoffMs = (long) (backoffMs * BACKOFF_MULTIPLIER);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("[Retry Handler] Thread interrompida durante backoff");
                        break;
                    }
                }
            }
        }

        log.error("[Retry Handler] Ferramenta {} falhou após {} tentativas: {}",
                toolName, MAX_RETRIES, lastException != null ? lastException.getMessage() : "unknown error");

        return RetryResult.failed(MAX_RETRIES, lastException);
    }
}
