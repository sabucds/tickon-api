package com.tickon.identity.user.infrastructure.security;

import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordHasher implements PasswordHasher {

  private final Argon2PasswordEncoder encoder;

  public Argon2PasswordHasher() {
    this.encoder = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);

  }

  @Override
  public PasswordHash hash(String rawPassword) {
    String hashedPassword = encoder.encode(rawPassword);
    return new PasswordHash(hashedPassword);
  }

  @Override
  public boolean verify(String rawPassword, PasswordHash hash) {
    return encoder.matches(rawPassword, hash.value());
  }
}
