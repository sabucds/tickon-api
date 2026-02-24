package com.tickon.identity.auth.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;
import com.tickon.identity.auth.infrastructure.web.dto.LogoutRequest;
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

  private final CommandBus commandBus;

  AuthController(CommandBus commandBus) {
    this.commandBus = commandBus;
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return LoginMapper.toLoginResponse(commandBus.execute(LoginMapper.toLoginCommand(request)).orElseThrow());
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/refresh")
  public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return LoginMapper.toLoginResponse(commandBus.execute(LoginMapper.toRefreshTokenCommand(request)).orElseThrow());
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PostMapping("/logout")
  public void logout(@Valid @RequestBody LogoutRequest request) {
    commandBus.execute(LoginMapper.toLogoutCommand(request));
  }
}
