package com.tickon.identity.auth.application.ports.out;

import com.tickon.identity.user.domain.valueobjects.Email;

public interface EmailSender {
  void sendPasswordResetEmail(Email to, String resetToken, String recipientName);
}
