package com.tickon.identity.user.application.command.delete;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeleteUserCommandHandler implements CommandHandler<DeleteUserCommand, Void> {
  private static final Logger log = LoggerFactory.getLogger(DeleteUserCommandHandler.class);

  private final UserRepository userRepository;
  private final IdentityMetrics metrics;

  public DeleteUserCommandHandler(UserRepository userRepository, IdentityMetrics metrics) {
    this.userRepository = userRepository;
    this.metrics = metrics;
  }

  @Override
  public CommandResult<Void> handle(DeleteUserCommand command) {
    User user = userRepository.findById(command.userId())
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + command.userId()));
    userRepository.delete(user.id());
    log.info("User deleted: userId={}", command.userId().value());
    metrics.userDeleted().increment();
    return new CommandResult.Success<>(null);
  }

  @Override
  public Class<DeleteUserCommand> getCommandClass() {
    return DeleteUserCommand.class;
  }
}
