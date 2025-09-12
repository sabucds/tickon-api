// user/application/use-cases/GetUserById.java
package com.tickon.identityservice.user.application.services;

import java.util.Optional;
import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdUseCase;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.UserId;

public class GetUserById implements GetUserByIdUseCase {

  private final UserRepository userRepository;

  public GetUserById(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<UserResponse> handle(UserId userId) {
    return userRepository.findById(userId)
        .map(UserResponse::from);
  }
}
