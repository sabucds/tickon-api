package com.tickon.identityservice.user.application.usecases;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdService;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.valueobjects.UserId;
import java.util.Optional;

public class GetUserById implements GetUserByIdService {

  private final UserRepository userRepository;

  public GetUserById(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserResponseModel> handle(String userId) {
    UserId id = UserId.from(userId);
    return userRepository.findById(id).map(UserResponseModel::from);
  }
}
