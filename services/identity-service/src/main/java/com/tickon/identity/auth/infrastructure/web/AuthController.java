package com.tickon.identity.auth.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.exceptions.GlobalExceptionHandler.ApiError;
import com.tickon.identity.auth.infrastructure.web.dto.LoginRequest;
import com.tickon.identity.auth.infrastructure.web.dto.LoginResponse;
import com.tickon.identity.auth.infrastructure.web.dto.LogoutRequest;
import com.tickon.identity.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.tickon.identity.auth.infrastructure.web.dto.RefreshTokenResponse;
import com.tickon.identity.auth.infrastructure.web.mappers.LoginMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Login, token refresh and logout")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final CommandBus commandBus;

  AuthController(CommandBus commandBus) {
    this.commandBus = commandBus;
  }

  @Operation(summary = "Login with email and password", security = {})
  @ApiResponse(responseCode = "200", description = "Successful login")
  @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED — request body is invalid",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS — wrong email or password",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return LoginMapper.toLoginResponse(commandBus.execute(LoginMapper.toLoginCommand(request)).orElseThrow());
  }

  @Operation(summary = "Refresh access token using a valid refresh token", security = {})
  @ApiResponse(responseCode = "200", description = "New token pair issued")
  @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED — request body is invalid",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "401",
      description = "INVALID_REFRESH_TOKEN | SESSION_EXPIRED | SESSION_REVOKED",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/refresh")
  public RefreshTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return LoginMapper
        .toRefreshTokenResponse(commandBus.execute(LoginMapper.toRefreshTokenCommand(request)).orElseThrow());
  }

  @Operation(summary = "Logout and revoke the current session", security = {})
  @ApiResponse(responseCode = "204", description = "Session revoked successfully")
  @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED — request body is invalid",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PostMapping("/logout")
  public void logout(@Valid @RequestBody LogoutRequest request) {
    commandBus.execute(LoginMapper.toLogoutCommand(request));
  }
}
