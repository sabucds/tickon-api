package com.tickon.identity.auth.application.ports.in;

import com.tickon.identity.auth.application.dto.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.dto.RequestPasswordResetResult;

public interface RequestPasswordResetUseCase {
  RequestPasswordResetResult requestPasswordReset(RequestPasswordResetCommand command);
}
