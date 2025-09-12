// user/application/use-cases/GetUserById.java
package com.tickon.identityservice.user.application.services;

import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdUseCase;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.UserId;
import java.util.Optional;

public class GetUserById implements GetUserByIdUseCase {

  private final UserRepository userRepository;

  public GetUserById(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<UserResponse> handle(UserId userId) {
    return userRepository.findById(userId).map(UserResponse::from);
  }
}
