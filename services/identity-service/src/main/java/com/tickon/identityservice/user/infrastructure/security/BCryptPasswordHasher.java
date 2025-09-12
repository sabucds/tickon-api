package com.tickon.identityservice.user.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.domain.PasswordHash;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

  private final BCryptPasswordEncoder encoder;

  public BCryptPasswordHasher() {
    this.encoder = new BCryptPasswordEncoder();
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
