package com.tickon.identity.auth.infrastructure.web.mappers;

import com.tickon.identity.auth.application.command.requestpasswordreset.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.command.resetpassword.ResetPasswordCommand;
import com.tickon.identity.auth.application.query.verifyresettoken.VerifyResetTokenQuery;
import com.tickon.identity.auth.infrastructure.web.dto.ForgotPasswordRequest;
import com.tickon.identity.auth.infrastructure.web.dto.ResetPasswordRequest;

public class PasswordResetMapper {

  private PasswordResetMapper() {}

  public static RequestPasswordResetCommand toRequestPasswordResetCommand(ForgotPasswordRequest request) {
    return new RequestPasswordResetCommand(request.email());
  }

  public static VerifyResetTokenQuery toVerifyResetTokenQuery(String token) {
    return new VerifyResetTokenQuery(token);
  }

  public static ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest request) {
    return new ResetPasswordCommand(request.token(), request.newPassword());
  }
}
