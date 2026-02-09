package com.tickon.identity.auth.infrastructure.web;

import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import com.tickon.identity.auth.application.ports.in.RefreshTokenUseCase;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;
import com.tickon.identity.auth.infrastructure.web.dto.RefreshTokenRequest;
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
  private final RefreshTokenUseCase refreshToken;

  AuthController(LoginUseCase loginUser, RefreshTokenUseCase refreshToken) {
    this.loginUser = loginUser;
    this.refreshToken = refreshToken;
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    var appResponse = loginUser.login(LoginMapper.toLoginCommand(request));
    return LoginMapper.toLoginResponse(appResponse);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/refresh")
  public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
    var appResponse = refreshToken.refresh(LoginMapper.toRefreshTokenCommand(request));
    return LoginMapper.toLoginResponse(appResponse);
  }

}
