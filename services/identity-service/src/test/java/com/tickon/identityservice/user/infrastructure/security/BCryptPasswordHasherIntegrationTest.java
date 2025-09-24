package com.tickon.identityservice.user.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickon.identityservice.user.domain.valueobjects.PasswordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BCryptPasswordHasherIntegrationTest {

  private BCryptPasswordHasher hasher;

  @BeforeEach
  void setUp() {
    hasher = new BCryptPasswordHasher();
  }

  @Test
  void shouldCreateAHashedPassword() {
    String rawPassword = "mySecretPassword";

    PasswordHash passwordHash = hasher.hash(rawPassword);

    assertNotNull(passwordHash);
    assertNotEquals(passwordHash.value(), rawPassword);
    assertTrue(passwordHash.value().startsWith("$2a$"));
  }

  @Test
  void shouldVerifyAHashedPassword() {
    String rawPassword = "mySecretPassword";

    PasswordHash passwordHash = hasher.hash(rawPassword);

    assertTrue(hasher.verify(rawPassword, passwordHash));
  }

  @Test
  void shouldFailVerification_WithWrongPassword() {
    String rawPassword = "mySecretPassword";
    String wrongPassword = "wrongPassword";

    PasswordHash passwordHash = hasher.hash(rawPassword);

    assertFalse(hasher.verify(wrongPassword, passwordHash));
  }

  @Test
  void shouldGenerateDifferentHashes_ForSamePassword() {
    String rawPassword = "mySecretPassword";

    PasswordHash passwordHash1 = hasher.hash(rawPassword);
    PasswordHash passwordHash2 = hasher.hash(rawPassword);

    assertNotEquals(passwordHash1.value(), passwordHash2.value());
    assertTrue(hasher.verify(rawPassword, passwordHash1));
    assertTrue(hasher.verify(rawPassword, passwordHash2));
  }
}
