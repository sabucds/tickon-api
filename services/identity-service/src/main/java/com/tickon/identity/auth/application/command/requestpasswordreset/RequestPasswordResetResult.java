package com.tickon.identity.auth.application.command.requestpasswordreset;

public record RequestPasswordResetResult(String message) {
  public static RequestPasswordResetResult success() {
    return new RequestPasswordResetResult("If the email exists in our system, a password reset link has been sent.");
  }
}
