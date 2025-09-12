package com.tickon.identityservice.user.application.ports.inbound;

import java.util.Optional;
import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.domain.UserId;

public interface GetUserByIdUseCase {
  Optional<UserResponse> handle(UserId userId);
}
