package com.tickon.identity.auth.application.ports;

public interface EmailSender {
  void sendPasswordResetEmail(String to, String resetToken, String recipientName);
}
