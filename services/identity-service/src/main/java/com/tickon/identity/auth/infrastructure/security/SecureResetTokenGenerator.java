package com.tickon.identity.auth.infrastructure.security;

import com.tickon.identity.auth.application.ports.out.ResetTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureResetTokenGenerator implements ResetTokenGenerator {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 32; // 256 bits

  @Override
  public String generateSecureToken() {
    byte[] randomBytes = new byte[TOKEN_BYTES];
    SECURE_RANDOM.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }
}
