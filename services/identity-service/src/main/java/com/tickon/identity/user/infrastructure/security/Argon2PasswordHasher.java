package com.tickon.identity.user.infrastructure.security;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.identity.shared.kernel.ports.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2PasswordHasher implements PasswordHasher {

  private final Argon2PasswordEncoder encoder;

  public Argon2PasswordHasher() {
    this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Override
  public PasswordHash hash(String rawPassword) {
    return PasswordHash.from(encoder.encode(rawPassword));
  }

  @Override
  public boolean verify(String rawPassword, PasswordHash hash) {
    return encoder.matches(rawPassword, hash.value());
  }
}
