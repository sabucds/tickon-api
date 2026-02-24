package com.tickon.identity.auth.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandResult;
import com.tickon.identity.auth.application.LoginResult;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LogoutRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CommandBus commandBus;

  @Test
  void shouldLoginAndReturnTokens_WhenValidRequest() throws Exception {
    LoginRequest request = new LoginRequest("john@example.com", "plain-password", "device-123");
    LoginResult response = new LoginResult("access-token", "refresh-token");

    when(commandBus.execute(any())).thenReturn(new CommandResult.Success<>(response));

    mockMvc
        .perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
  }

  @Test
  void shouldReturnBadRequest_WhenPayloadIsInvalid() throws Exception {
    LoginRequest request = new LoginRequest("", "", "");

    mockMvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnNoContent_WhenValidLogoutRequest() throws Exception {
    LogoutRequest request = new LogoutRequest("valid-refresh-token");

    when(commandBus.execute(any())).thenReturn(new CommandResult.Success<>(null));

    mockMvc.perform(post("/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andExpect(status().isNoContent());

    verify(commandBus).execute(any());
  }

  @Test
  void shouldReturnBadRequest_WhenLogoutRequestIsInvalid() throws Exception {
    LogoutRequest request = new LogoutRequest("");

    mockMvc.perform(post("/v1/auth/logout").contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

    verify(commandBus, never()).execute(any());
  }
}
