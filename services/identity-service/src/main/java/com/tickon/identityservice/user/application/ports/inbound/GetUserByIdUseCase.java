package com.tickon.identityservice.user.application.ports.inbound;

import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.domain.UserId;
import java.util.Optional;

public interface GetUserByIdUseCase {
  Optional<UserResponse> handle(UserId userId);
}
