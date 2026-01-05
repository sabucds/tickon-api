package com.tickon.identity.user.application.services;

import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.GetUserByIdUseCase;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GetUserByIdService implements GetUserByIdUseCase {

  private final UserRepository userRepository;

  public GetUserByIdService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserResult> handle(String userId) {
    UserId id = UserId.from(userId);
    return userRepository.findById(id).map(UserResult::from);
  }
}
