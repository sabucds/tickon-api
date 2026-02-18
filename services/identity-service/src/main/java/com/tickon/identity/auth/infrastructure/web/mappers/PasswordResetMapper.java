package com.tickon.identity.auth.infrastructure.web.mappers;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.identity.auth.application.dto.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.dto.ResetPasswordCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.ResetPasswordRequest;

public class PasswordResetMapper {

  private PasswordResetMapper() {}

  public static RequestPasswordResetCommand toRequestPasswordResetCommand(ForgotPasswordRequest request) {
    return new RequestPasswordResetCommand(Email.from(request.email()));
  }

  public static VerifyResetTokenCommand toVerifyResetTokenCommand(String token) {
    return new VerifyResetTokenCommand(token);
  }

  public static ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest request) {
    return new ResetPasswordCommand(request.token(), request.newPassword());
  }
}
