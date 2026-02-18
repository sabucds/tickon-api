package com.tickon.identity.user.application.services;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.GetUserByIdUseCase;
import com.tickon.identity.user.application.ports.out.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetUserByIdService implements GetUserByIdUseCase {

  private static final Logger log = LoggerFactory.getLogger(GetUserByIdService.class);

  private final UserRepository userRepository;

  public GetUserByIdService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<UserResult> handle(UserId userId) {
    Optional<UserResult> result = userRepository.findById(userId).map(UserResult::from);
    if (result.isPresent()) {
      log.debug("User found: userId={}", userId.value());
    } else {
      log.warn("User not found: userId={}", userId.value());
    }
    return result;
  }
}
