package com.tickon.identityservice.user.application.ports.inbound;

import com.tickon.identityservice.user.application.models.UserResponseModel;

import java.util.Optional;

public interface GetUserByIdService {
  Optional<UserResponseModel> handle(String userId);
}
