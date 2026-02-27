package com.tickon.identity.auth.application.ports;

public interface ResetTokenGenerator {
  String generateSecureToken();
}
