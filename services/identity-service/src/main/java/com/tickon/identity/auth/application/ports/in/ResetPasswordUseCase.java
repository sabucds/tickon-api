package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.ResetPasswordCommand;

public interface ResetPasswordUseCase {
  void resetPassword(ResetPasswordCommand command);
}
