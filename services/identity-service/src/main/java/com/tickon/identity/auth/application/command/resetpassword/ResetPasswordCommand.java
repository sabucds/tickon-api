package com.tickon.identity.auth.application.command.resetpassword;

import com.tickon.common.commands.Command;

public record ResetPasswordCommand(String resetToken, String newPassword) implements Command<Void> {
  @Override
  public String toString() {
    return "ResetPasswordCommand[resetToken=***REDACTED***, newPassword=***REDACTED***]";
  }
}
