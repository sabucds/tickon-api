package com.tickon.identity.user.infrastructure.security;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Primary
public class Argon2PasswordHasher
    implements PasswordHasher, com.tickon.identity.auth.application.ports.out.PasswordHasher {

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
