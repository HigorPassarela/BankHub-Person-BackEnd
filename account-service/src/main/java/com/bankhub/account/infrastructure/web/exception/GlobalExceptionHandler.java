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

    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurityException(SecurityException ex) {
        log.warn("Exceção de Segurança tratada (403): {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Acesso Negado");
        problemDetail.setType(URI.create("https://bank-hub.com/errors/forbidden"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Conta não Encontrada");
        return problemDetail;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ProblemDetail handleBusinessExceptions(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Requisição Inválida");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro interno do servidor: ", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado.");
        problemDetail.setTitle("Erro Interno");
        return problemDetail;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFundsException(InsufficientFundsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setTitle("Saldo Insuficiente");
        return problemDetail;
    }
}
