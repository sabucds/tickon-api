package com.tickon.identityservice.user.application.ports.inbound;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.domain.valueobjects.UserId;

import java.util.Optional;

public interface GetUserByIdService {
  Optional<UserResponseModel> handle(UserId userId);
}
