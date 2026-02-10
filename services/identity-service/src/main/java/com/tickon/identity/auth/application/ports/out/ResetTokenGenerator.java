package com.tickon.identity.auth.application.ports.out;

public interface ResetTokenGenerator {
  String generateSecureToken();
}
