package com.tickon.identityservice.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PasswordHashTest {
  @Test
  void shouldCreatePassword_WhenValid() {
    String validPassword = "my-hashed-password";
    PasswordHash password = new PasswordHash(validPassword);
    assertThat(password.value()).isEqualTo(validPassword);
  }

  @Test
  void shouldThrowException_WhenNullPassword() {
    assertThatThrownBy(() -> new PasswordHash(null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_WhenEmptyPassword() {
    assertThatThrownBy(() -> new PasswordHash("")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowException_WhenBlankPassword() {
    assertThatThrownBy(() -> new PasswordHash("   ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldBeEqual_WhenSameHash() {
    String hash = "$2a$10$example.hash.value";
    PasswordHash password1 = new PasswordHash(hash);
    PasswordHash password2 = new PasswordHash(hash);
    assertThat(password1).isEqualTo(password2);
    assertThat(password1.hashCode()).hasSameHashCodeAs(password2.hashCode());
  }

  @Test
  void shouldNotBeEqual_WhenDifferentHash() {
    PasswordHash password1 = new PasswordHash("hash1");
    PasswordHash password2 = new PasswordHash("hash2");
    assertThat(password1).isNotEqualTo(password2);
  }
}
