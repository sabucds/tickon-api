package com.tickon.identity.user.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Argon2PasswordHasherIntegrationTest {

  private Argon2PasswordHasher hasher;

  @BeforeEach
  void setUp() {
    hasher = new Argon2PasswordHasher();
  }

  @Test
  void shouldCreateAHashedPassword() {
    String rawPassword = "mySecretPassword";

    String passwordHash = hasher.hash(rawPassword);

    assertNotNull(passwordHash);
    assertNotEquals(passwordHash, rawPassword);
    assertTrue(passwordHash.startsWith("$argon2id$"));
  }

  @Test
  void shouldVerifyAHashedPassword() {
    String rawPassword = "mySecretPassword";

    String passwordHash = hasher.hash(rawPassword);

    assertTrue(hasher.verify(rawPassword, passwordHash));
  }

  @Test
  void shouldFailVerification_WithWrongPassword() {
    String rawPassword = "mySecretPassword";
    String wrongPassword = "wrongPassword";

    String passwordHash = hasher.hash(rawPassword);

    assertFalse(hasher.verify(wrongPassword, passwordHash));
  }

  @Test
  void shouldGenerateDifferentHashes_ForSamePassword() {
    String rawPassword = "mySecretPassword";

    String passwordHash1 = hasher.hash(rawPassword);
    String passwordHash2 = hasher.hash(rawPassword);

    assertNotEquals(passwordHash1, passwordHash2);
    assertTrue(hasher.verify(rawPassword, passwordHash1));
    assertTrue(hasher.verify(rawPassword, passwordHash2));
  }
}
