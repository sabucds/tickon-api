package com.tickon.identity.user.application.ports.in;

import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.util.Optional;

public interface GetUserByIdUseCase {
  Optional<UserResult> handle(UserId userId);
}
