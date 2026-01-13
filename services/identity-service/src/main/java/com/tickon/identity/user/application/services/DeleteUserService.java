package com.tickon.identity.user.application.services;

import com.tickon.identity.user.application.ports.in.DeleteUserUseCase;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.valueobjects.UserId;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {

  private final UserRepository userRepository;

  public DeleteUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void handle(String userId) {
    UserId id = UserId.from(userId);
    userRepository.delete(id);
  }
}
