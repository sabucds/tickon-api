package com.tickon.identity.user.application.ports.in;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.application.dto.UserResult;
import java.util.Optional;

public interface GetUserByIdUseCase {
  Optional<UserResult> handle(UserId userId);
}
