package com.tickon.identity.auth.application.ports.out;

public interface EmailSender {
  void sendPasswordResetEmail(String to, String resetToken, String recipientName);
}
