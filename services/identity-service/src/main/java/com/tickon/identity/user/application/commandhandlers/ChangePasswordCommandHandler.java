package com.tickon.identity.user.application.commandhandlers;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.identity.auth.application.ports.out.PasswordHasher;
import com.tickon.identity.shared.contracts.commands.ChangePasswordCommand;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordCommandHandler implements CommandHandler<ChangePasswordCommand, Void> {
  private static final Logger log = LoggerFactory.getLogger(ChangePasswordCommandHandler.class);

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;

  public ChangePasswordCommandHandler(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  public CommandResult<Void> handle(ChangePasswordCommand command) {
    log.debug("Changing password for user: {}", command.userId().value());

    passwordPolicy.validate(command.newPlainPassword());
    PasswordHash hashedPassword = passwordHasher.hash(command.newPlainPassword());

    User user = userRepository.findById(command.userId())
        .orElseThrow(() -> new IllegalStateException("User not found: " + command.userId().value()));

    user.changePasswordHash(hashedPassword);
    userRepository.save(user);

    log.info("Password changed successfully for user: {}", command.userId().value());
    return new CommandResult.Success<>(null);
  }

  @Override
  public Class<ChangePasswordCommand> getCommandClass() {
    return ChangePasswordCommand.class;
  }
}
