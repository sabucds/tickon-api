package com.tickon.identity.auth.application.ports.out;

import com.tickon.common.identity.domain.valueobjects.Email;

public interface EmailSender {
  void sendPasswordResetEmail(Email to, String resetToken, String recipientName);
}
