package com.tickon.identity.contracts.user.commands;

import com.tickon.common.commands.Command;
import java.util.UUID;

// Cross-module contract: dispatched by auth/ to user/ via CommandBus.
// Intra-module commands stay inside their own application/command/ package.
public record ChangePasswordCommand(UUID userId, String newPlainPassword) implements Command<Void> {

  public ChangePasswordCommand {
    if (userId == null) {
      throw new IllegalArgumentException("userId cannot be null");
    }
    if (newPlainPassword == null || newPlainPassword.isBlank()) {
      throw new IllegalArgumentException("newPlainPassword cannot be null or blank");
    }
  }

  @Override
  public String getCommandName() {
    return "ChangePassword.v1";
  }
}
