package com.tickon.identityservice.user.infrastructure.web;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdService;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.infrastructure.web.dto.RegisterUserRequest;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean RegisterUserService registerUserService;
  @MockBean GetUserByIdService getUserByIdService;

  List<User> users = new ArrayList<>();

  @BeforeEach
  void setUp(){
    // users = List.of(
    //   User.forRegistration(UserId.from("1"), Email.from("sabrina@gmail.com"), Username.from("sabucds"), "Sabrina", "Correia", new PasswordHash("MySecure.123"), Instant.now()),
    //   User.forRegistration(UserId.from("2"), Email.from("sabrina1@gmail.com"), Username.from("sabucds1"), "Sabrina", "Correia", new PasswordHash("MySecure.1234"), Instant.now())
    // );
  }

  @Test
  public void shouldRegisterANewUser() throws Exception {
    // Given
    RegisterUserRequest registerUserBody = new RegisterUserRequest("Sabrina","Correia", "sabucds", "sabueskul@gmail.com", "MyPass!23@");
    UserResponseModel expectedResponse = new UserResponseModel("dd767785-8f52-400b-aa98-719d1cc2e1ba", "sabucds", "sabueskul@gmail.com", "Sabrina", "Correia");
    
    when(registerUserService.register(any(RegisterUserService.RegisterUserCommand.class)))
        .thenReturn(expectedResponse);
    
    String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(registerUserBody);
    
    // When & Then
    mockMvc.perform(
      post("/v1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonBody)
    ).andExpect(status().isOk())
     .andExpect(jsonPath("$.id").value("dd767785-8f52-400b-aa98-719d1cc2e1ba"))
     .andExpect(jsonPath("$.username").value("sabucds"))
     .andExpect(jsonPath("$.email").value("sabueskul@gmail.com"))
     .andExpect(jsonPath("$.firstName").value("Sabrina"))
     .andExpect(jsonPath("$.lastName").value("Correia"));
  }
  
}
