package com.tickon.identity.shared.contracts.commands;

import com.tickon.common.commands.Command;
import com.tickon.common.identity.domain.valueobjects.UserId;

public record ChangePasswordCommand(UserId userId, String newPlainPassword) implements Command<Void> {

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
