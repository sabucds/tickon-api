package com.tickon.identity.user.application.command.register;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.out.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserCommandHandler implements CommandHandler<RegisterUserCommand, UserResult> {
  private static final Logger log = LoggerFactory.getLogger(RegisterUserCommandHandler.class);

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public RegisterUserCommandHandler(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy, DomainEventPublisher eventPublisher, IdentityMetrics metrics) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  public CommandResult<UserResult> handle(RegisterUserCommand cmd) {
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
    PasswordHash hash = PasswordHash.from(passwordHasher.hash(cmd.rawPassword()));

    User user = User.create(UserId.generate(), cmd.email(), cmd.username(), cmd.firstName(), cmd.lastName(), hash);
    userRepository.save(user);
    eventPublisher.publishAll(user.domainEvents());
    user.clearEvents();

    log.info("User registered: userId={}, username='{}'", user.id().value(), cmd.username().value());
    metrics.userRegistered().increment();

    return new CommandResult.Success<>(UserResult.from(user));
  }

  @Override
  public Class<RegisterUserCommand> getCommandClass() {
    return RegisterUserCommand.class;
  }
}
