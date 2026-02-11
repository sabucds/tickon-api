package com.tickon.identity.auth.application.dto;

public record ResetPasswordCommand(String resetToken, String newPassword) {
  @Override
  public String toString() {
    return "ResetPasswordCommand[resetToken=***REDACTED***, newPassword=***REDACTED***]";
  }
}
