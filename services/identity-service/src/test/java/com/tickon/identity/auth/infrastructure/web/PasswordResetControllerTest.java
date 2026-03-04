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
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.query.verifyresettoken.VerifyResetTokenResult;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.ResetPasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CommandBus commandBus;

  @MockBean
  private QueryBus queryBus;

  // --- /request ---

  @Test
  void shouldReturnOk_WhenForgotPasswordRequestIsValid() throws Exception {
    ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");
    when(commandBus.execute(any())).thenReturn(new CommandResult.Success<>(null));

    mockMvc
        .perform(post("/v1/auth/password-reset/request").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value(
            "If an account exists with this email, you will receive password reset instructions."));
  }

  @Test
  void shouldReturnBadRequest_WhenForgotPasswordEmailIsBlank() throws Exception {
    ForgotPasswordRequest request = new ForgotPasswordRequest("");

    mockMvc
        .perform(post("/v1/auth/password-reset/request").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(commandBus, never()).execute(any());
  }

  @Test
  void shouldReturnBadRequest_WhenForgotPasswordEmailIsInvalid() throws Exception {
    ForgotPasswordRequest request = new ForgotPasswordRequest("not-an-email");

    mockMvc
        .perform(post("/v1/auth/password-reset/request").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(commandBus, never()).execute(any());
  }

  // --- /verify ---

  @Test
  void shouldReturnValidTrue_WhenTokenIsValid() throws Exception {
    when(queryBus.execute(any())).thenReturn(new QueryResult.Success<>(new VerifyResetTokenResult(true)));

    mockMvc.perform(post("/v1/auth/password-reset/verify").param("token", "valid-token"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));
  }

  @Test
  void shouldReturnValidFalse_WhenTokenIsExpired() throws Exception {
    when(queryBus.execute(any())).thenReturn(new QueryResult.Success<>(new VerifyResetTokenResult(false)));

    mockMvc.perform(post("/v1/auth/password-reset/verify").param("token", "expired-token"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false));
  }

  // --- /reset ---

  @Test
  void shouldReturnOk_WhenResetPasswordRequestIsValid() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "NewPassword123!");
    when(commandBus.execute(any())).thenReturn(new CommandResult.Success<>(null));

    mockMvc
        .perform(post("/v1/auth/password-reset/reset").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(commandBus).execute(any());
  }

  @Test
  void shouldReturnBadRequest_WhenResetPasswordRequestIsInvalid() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest("", "");

    mockMvc
        .perform(post("/v1/auth/password-reset/reset").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(commandBus, never()).execute(any());
  }
}
