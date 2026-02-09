package com.tickon.identity.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(name = OpenApiConfig.SECURITY_SCHEME_NAME, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

  public static final String SECURITY_SCHEME_NAME = "bearerAuth";

  @Bean
  OpenAPI openApi(@Value("${app.swagger.server-url:/api/identity}") String serverUrl) {
    io.swagger.v3.oas.models.security.SecurityScheme securityScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
        .name(SECURITY_SCHEME_NAME).type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP).scheme("bearer")
        .bearerFormat("JWT");

    return new OpenAPI().components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .servers(List.of(new Server().url(serverUrl)));
  }
}
