package com.tickon.identity.auth.infrastructure.web;

import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;
import com.tickon.identity.auth.infrastructure.web.mappers.LoginMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final LoginUseCase loginUser;

  AuthController(LoginUseCase loginUser) {

    this.loginUser = loginUser;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    var appResponse = loginUser.login(LoginMapper.toLoginCommand(request));
    return LoginMapper.toLoginResponse(appResponse);
  }

}
