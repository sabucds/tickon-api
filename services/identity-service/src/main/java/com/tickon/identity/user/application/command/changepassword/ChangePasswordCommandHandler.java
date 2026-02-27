package com.tickon.identity.user.application.command.changepassword;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.identity.contracts.user.commands.ChangePasswordCommand;
import com.tickon.identity.shared.kernel.ports.PasswordHasher;
import com.tickon.identity.user.application.ports.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
  @Transactional
  public CommandResult<Void> handle(ChangePasswordCommand command) {
    passwordPolicy.validate(command.newPlainPassword());
    PasswordHash hashedPassword = PasswordHash.from(passwordHasher.hash(command.newPlainPassword()));

    UserId userId = new UserId(command.userId());
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalStateException("User not found: " + command.userId()));

    user.changePasswordHash(hashedPassword);
    userRepository.save(user);

    log.info("Password changed successfully for user: {}", command.userId());
    return new CommandResult.Success<>(null);
  }

  @Override
  public Class<ChangePasswordCommand> getCommandClass() {
    return ChangePasswordCommand.class;
  }
}
