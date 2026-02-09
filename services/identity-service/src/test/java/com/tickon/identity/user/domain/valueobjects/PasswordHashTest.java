package com.tickon.identity.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordHashTest {

  @ParameterizedTest
  @ValueSource(strings = { "my-hashed-password", "$2a$10$example.hash.value" })
  void shouldCreatePassword_WhenValid(String validPassword) {
    PasswordHash password = new PasswordHash(validPassword);
    assertThat(password.value()).isEqualTo(validPassword);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "   " })
  void shouldThrowException_WhenNullEmptyOrBlank(String invalidPassword) {
    assertThatThrownBy(() -> new PasswordHash(invalidPassword)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = { "hash1", "hash2" })
  void equalityShouldDependOnHashValue(String hash) {
    PasswordHash password1 = new PasswordHash(hash);
    PasswordHash password2 = new PasswordHash(hash);
    assertThat(password1).isEqualTo(password2);
    assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
  }

  @ParameterizedTest
  @ValueSource(strings = { "hash1:hash2", "hashA:hashB" })
  void shouldNotBeEqual_WhenDifferentHash(String hashes) {
    String[] parts = hashes.split(":");
    PasswordHash password1 = new PasswordHash(parts[0]);
    PasswordHash password2 = new PasswordHash(parts[1]);
    assertThat(password1).isNotEqualTo(password2);
  }
}
