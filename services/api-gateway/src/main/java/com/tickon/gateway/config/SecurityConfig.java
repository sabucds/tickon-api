package com.tickon.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .pathMatchers(HttpMethod.POST, "/api/identity/v1/auth/login", "/api/identity/v1/users",
                "/api/identity/v1/auth/password-reset/request", "/api/identity/v1/auth/password-reset/verify",
                "/api/identity/v1/auth/password-reset/reset")
            .permitAll()
            .pathMatchers("/api/identity/swagger-ui/**", "/api/identity/docs/**", "/api/identity/v3/api-docs/**",
                "/api/identity/v3/api-docs")
            .permitAll()
            .pathMatchers("/actuator/health", "/actuator/info", "/actuator/metrics", "/actuator/prometheus",
                "/actuator/gateway/**")
            .permitAll().anyExchange().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())).build();
  }
}
