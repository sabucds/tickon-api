package com.tickon.identity.user.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.DeleteUserUseCase;
import com.tickon.identity.user.application.ports.in.GetUserByIdUseCase;
import com.tickon.identity.user.application.ports.in.RegisterUserUseCase;
import com.tickon.identity.user.infrastructure.web.dto.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private RegisterUserUseCase registerUserService;

  @MockBean
  private GetUserByIdUseCase getUserByIdService;

  @MockBean
  private DeleteUserUseCase deleteUserUseCase;

  @Test
  void shouldRegisterNewUser_WhenValidInput() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest("John", "Doe", "johndoe", "john@example.com",
        "SecurePass123!");
    UserResult expectedResponse = new UserResult("123", "johndoe", "john@example.com", "John", "Doe");

    when(registerUserService.register(any(RegisterUserCommand.class))).thenReturn(expectedResponse);

    mockMvc
        .perform(
            post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("123"))
        .andExpect(jsonPath("$.username").value("johndoe")).andExpect(jsonPath("$.email").value("john@example.com"))
        .andExpect(jsonPath("$.firstName").value("John")).andExpect(jsonPath("$.lastName").value("Doe"));
  }

  @Test
  void shouldReturnBadRequest_WhenFirstNameIsBlank() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest("", "Doe", "johndoe", "john@example.com", "SecurePass123!");

    mockMvc
        .perform(
            post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.firstName").value("First name is required"));
  }

  @Test
  void shouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest("John", "Doe", "johndoe", "invalid-email", "SecurePass123!");

    mockMvc
        .perform(
            post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").value("Email must be valid"));
  }

  @Test
  void shouldReturnBadRequest_WhenPasswordTooShort() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest("John", "Doe", "johndoe", "john@example.com", "123");

    mockMvc
        .perform(
            post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").value("Password must be at least 8 characters"));
  }

  @Test
  void shouldReturnBadRequest_WhenMultipleValidationErrors() throws Exception {
    RegisterUserRequest request = new RegisterUserRequest("", "", "ab", "invalid-email", "123");

    mockMvc
        .perform(
            post("/v1/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.firstName").exists()).andExpect(jsonPath("$.errors.lastName").exists())
        .andExpect(jsonPath("$.errors.username").exists()).andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());
  }

}
