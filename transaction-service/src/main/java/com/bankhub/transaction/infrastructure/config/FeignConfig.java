package com.bankhub.transaction.infrastructure.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração global do Feign para todos os clientes HTTP.
 * Define timeouts e retry policy.
 */
@Configuration
public class FeignConfig {

    /**
     * Timeout configuration:
     * - Connect timeout: 3 segundos
     * - Read timeout: 10 segundos (operações normais)
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                3, TimeUnit.SECONDS,  // connect timeout
                10, TimeUnit.SECONDS,  // read timeout
                true  // followRedirects
        );
    }

    /**
     * Retry policy: 3 tentativas com backoff exponencial.
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                100L,  // initial interval (ms)
                2000L, // max interval (ms)
                3      // max attempts
        );
    }
}
