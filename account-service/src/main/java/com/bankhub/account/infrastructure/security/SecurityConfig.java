package com.bankhub.account.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Custom JWT decoder com suporte a múltiplos issuers para ambiente Docker.
     * <p>
     * Em ambientes containerizados, o Keycloak é acessível por dois URLs diferentes:
     * - http://keycloak:8080/realms/bankhub (acesso interno via Docker network)
     * - http://localhost:9000/realms/bankhub (acesso externo do host/browser)
     * <p>
     * Tokens gerados via browser possuem iss: http://localhost:9000/realms/bankhub
     * mas o serviço roda dentro do Docker e precisa aceitar ambos os issuers.
     * <p>
     * Este decoder configura validação que aceita ambos os issuers mantendo
     * validação rigorosa de assinatura e expiração do token.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Cria decoder usando JWK set do Keycloak para validação de assinatura
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri("http://keycloak:8080/realms/bankhub/protocol/openid-connect/certs")
                .build();

        // Validador customizado que aceita múltiplos issuers
        OAuth2TokenValidator<Jwt> multiIssuerValidator = token -> {
            String issuer = token.getIssuer().toString();
            if ("http://keycloak:8080/realms/bankhub".equals(issuer) ||
                "http://localhost:9000/realms/bankhub".equals(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "The iss claim is not valid", null)
            );
        };

        // Combina validadores: timestamp (expiração) + multi-issuer
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                multiIssuerValidator
        );

        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers("/api/v1/accounts/activate").permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
