package com.tickon.identity.auth.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.queries.QueryBus;
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

  private final CommandBus commandBus;
  private final QueryBus queryBus;

  PasswordResetController(CommandBus commandBus, QueryBus queryBus) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/request")
  public ForgotPasswordResponse requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
    commandBus.execute(PasswordResetMapper.toRequestPasswordResetCommand(request));
    return new ForgotPasswordResponse(
        "If an account exists with this email, you will receive password reset instructions.");
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/verify")
  public VerifyResetTokenResponse verifyToken(@RequestParam String token) {
    var result = queryBus.execute(PasswordResetMapper.toVerifyResetTokenQuery(token)).orElseThrow();
    return new VerifyResetTokenResponse(result.valid());
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/reset")
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    commandBus.execute(PasswordResetMapper.toResetPasswordCommand(request));
  }
}
