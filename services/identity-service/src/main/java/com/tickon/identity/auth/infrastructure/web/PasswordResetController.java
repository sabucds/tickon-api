package com.tickon.identity.auth.infrastructure.web;

import com.tickon.identity.auth.application.ports.in.RequestPasswordResetUseCase;
import com.tickon.identity.auth.application.ports.in.ResetPasswordUseCase;
import com.tickon.identity.auth.application.ports.in.VerifyResetTokenUseCase;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordResponse;
import com.tickon.identity.auth.infrastructure.web.dto.ResetPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.VerifyResetTokenResponse;
import com.tickon.identity.auth.infrastructure.web.mappers.PasswordResetMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/password-reset")
public class PasswordResetController {

  private final RequestPasswordResetUseCase requestPasswordReset;
  private final VerifyResetTokenUseCase verifyResetToken;
  private final ResetPasswordUseCase resetPassword;

  PasswordResetController(RequestPasswordResetUseCase requestPasswordReset, VerifyResetTokenUseCase verifyResetToken,
      ResetPasswordUseCase resetPassword) {
    this.requestPasswordReset = requestPasswordReset;
    this.verifyResetToken = verifyResetToken;
    this.resetPassword = resetPassword;
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/request")
  public ForgotPasswordResponse requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
    requestPasswordReset.requestPasswordReset(PasswordResetMapper.toRequestPasswordResetCommand(request));
    return new ForgotPasswordResponse(
        "If an account exists with this email, you will receive password reset instructions.");
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/verify")
  public VerifyResetTokenResponse verifyToken(@RequestParam String token) {
    var result = verifyResetToken.verifyResetToken(PasswordResetMapper.toVerifyResetTokenCommand(token));
    return new VerifyResetTokenResponse(result.valid());
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/reset")
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    resetPassword.resetPassword(PasswordResetMapper.toResetPasswordCommand(request));
  }
}
