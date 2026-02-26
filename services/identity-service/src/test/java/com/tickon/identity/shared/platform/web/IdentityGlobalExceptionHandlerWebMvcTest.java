package com.tickon.identity.shared.platform.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickon.common.commands.CommandBus;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.infrastructure.web.AuthController;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.bootstrap.config.MessageSourceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({IdentityGlobalExceptionHandler.class, MessageSourceConfig.class})
class IdentityGlobalExceptionHandlerWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CommandBus commandBus;

  @Test
  void shouldTranslateDomainErrorBasedOnAcceptLanguage() throws Exception {
    LoginRequest request = new LoginRequest("john@example.com", "plain-password", "device-123");

    when(commandBus.execute(any())).thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "es")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
  }
}
