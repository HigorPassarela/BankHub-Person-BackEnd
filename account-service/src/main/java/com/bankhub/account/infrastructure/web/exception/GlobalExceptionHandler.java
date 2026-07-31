package com.bankhub.account.infrastructure.web.exception;

import com.bankhub.account.domain.exception.AccountNotFoundException;
import com.bankhub.account.domain.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Captura exceções de negócio quando uma conta não é encontrada ou pertence a outro cliente.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {

        log.warn("Exceção de Domínio tratada (404): {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Conta não Encontrada");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Captura validações de domínio e regras de negócio (ex: Valores negativos, status inválido).
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ProblemDetail handleBusinessExceptions(RuntimeException ex) {
        log.warn("Exceção de Regra de Negócio tratada (400): {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Requisição Inválida");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Fallback (Catch-All) para erros inesperados, garantindo que a stacktrace nunca vaze (Lei 3.B).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro interno do servidor não tratado explicitamente: ", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Captura tentativas de débito sem saldo (PIX, Compras na Bolsa, Saques).
     * Retorna 422 Unprocessable Entity (Sintaxe correta, mas a regra de negócio não permite).
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFundsException(InsufficientFundsException ex) {
        log.warn("Exceção Contábil tratada (422): {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setTitle("Saldo Insuficiente");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/insufficient-funds"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    /**
     * Captura falhas de concorrência no MongoDB (@Version). Retorna HTTP 409 Conflict.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingException(OptimisticLockingFailureException ex) {
        log.warn("Exceção de Concorrência (Optimistic Locking) tratada (409): {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"A conta foi modificada por outra transação simultânea. Por favor, tente novamente.");
        problemDetail.setTitle("Conflito de Concorrência");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
