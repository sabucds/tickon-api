package com.tickon.identity.user.application.services;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.RegisterUserUseCase;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

  private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy, DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  public UserResult register(RegisterUserCommand cmd) {
    if (userRepository.existsByEmail(cmd.email())) {
      log.warn("Registration failed: duplicate email for username='{}'", cmd.username().value());
      metrics.registrationFailed("duplicate_email").increment();
      throw new DuplicateEmailException(cmd.email().value());
    }
    if (userRepository.existsByUsername(cmd.username())) {
      log.warn("Registration failed: duplicate username='{}'", cmd.username().value());
      metrics.registrationFailed("duplicate_username").increment();
      throw new DuplicateUsernameException(cmd.username().value());
    }

    passwordPolicy.validate(cmd.rawPassword());
    PasswordHash hash = passwordHasher.hash(cmd.rawPassword());

    User user = User.create(UserId.generate(), cmd.email(), cmd.username(), cmd.firstName(), cmd.lastName(), hash);
    userRepository.save(user);
    eventPublisher.publishAll(user.domainEvents());
    user.clearEvents();

    log.info("User registered: userId={}, username='{}'", user.id().value(), cmd.username().value());
    metrics.userRegistered().increment();

    return UserResult.from(user);
  }

}
