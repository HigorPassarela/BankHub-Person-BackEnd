package com.bankhub.hubia.infrastructure.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    /**
     * Hub-IA tem timeout estendido porque interage com LLM que pode demorar.
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                3, TimeUnit.SECONDS,  // connect timeout
                30, TimeUnit.SECONDS, // read timeout ESTENDIDO (LLM calls)
                true
        );
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100L, 2000L, 3);
    }
}
