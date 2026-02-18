package com.tickon.identity.user.application.services;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.user.application.ports.in.DeleteUserUseCase;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {

  private static final Logger log = LoggerFactory.getLogger(DeleteUserService.class);

  private final UserRepository userRepository;
  private final IdentityMetrics metrics;

  public DeleteUserService(UserRepository userRepository, IdentityMetrics metrics) {
    this.userRepository = userRepository;
    this.metrics = metrics;
  }

  @Override
  public void handle(UserId userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    userRepository.delete(user.id());
    log.info("User deleted: userId={}", userId.value());
    metrics.userDeleted().increment();
  }
}
