package com.tickon.identity.auth.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.exceptions.GlobalExceptionHandler.ApiError;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordResponse;
import com.tickon.identity.auth.infrastructure.web.dto.ResetPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.VerifyResetTokenResponse;
import com.tickon.identity.auth.infrastructure.web.mappers.PasswordResetMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Password Reset", description = "Request, verify and complete a password reset flow")
@RestController
@RequestMapping("/v1/auth/password-reset")
public class PasswordResetController {

  private final CommandBus commandBus;
  private final QueryBus queryBus;

  PasswordResetController(CommandBus commandBus, QueryBus queryBus) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @Operation(summary = "Request a password reset email", security = {},
      description = "Always returns 200 regardless of whether the email exists, to prevent user enumeration.")
  @ApiResponse(responseCode = "200", description = "Reset email sent if the account exists")
  @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED — request body is invalid",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/request")
  public ForgotPasswordResponse requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
    commandBus.execute(PasswordResetMapper.toRequestPasswordResetCommand(request));
    return new ForgotPasswordResponse(
        "If an account exists with this email, you will receive password reset instructions.");
  }

  @Operation(summary = "Verify a password reset token", security = {})
  @ApiResponse(responseCode = "200", description = "Token validity result")
  @ApiResponse(responseCode = "400", description = "INVALID_RESET_TOKEN — token is malformed or expired",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/verify")
  public VerifyResetTokenResponse verifyToken(@RequestParam String token) {
    var result = queryBus.execute(PasswordResetMapper.toVerifyResetTokenQuery(token)).orElseThrow();
    return new VerifyResetTokenResponse(result.valid());
  }

  @Operation(summary = "Reset password using a valid reset token", security = {})
  @ApiResponse(responseCode = "200", description = "Password reset successfully")
  @ApiResponse(responseCode = "400",
      description = "VALIDATION_FAILED | INVALID_RESET_TOKEN | INVALID_PASSWORD — see error code in response",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/reset")
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    commandBus.execute(PasswordResetMapper.toResetPasswordCommand(request));
  }
}
