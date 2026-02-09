package com.tickon.identity.user.application.services;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.application.ports.in.DeleteUserUseCase;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {

  private final UserRepository userRepository;

  public DeleteUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void handle(UserId userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    userRepository.delete(user.id());
  }
}
