package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;

public interface VerifyResetTokenUseCase {
  VerifyResetTokenResult verifyResetToken(VerifyResetTokenCommand command);
}
