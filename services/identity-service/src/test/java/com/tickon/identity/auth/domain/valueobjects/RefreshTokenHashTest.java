package com.tickon.identity.auth.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RefreshTokenHashTest {

  @ParameterizedTest
  @ValueSource(strings = { "my-hashed-refreshToken", "$2a$10$example.hash.value" })
  void shouldCreateRefreshToken_WhenValid(String validRefreshToken) {
    RefreshTokenHash refreshToken = new RefreshTokenHash(validRefreshToken);
    assertThat(refreshToken.value()).isEqualTo(validRefreshToken);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "   " })
  void shouldThrowException_WhenNullEmptyOrBlank(String invalidRefreshToken) {
    assertThatThrownBy(() -> new RefreshTokenHash(invalidRefreshToken)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = { "hash1", "hash2" })
  void equalityShouldDependOnHashValue(String hash) {
    RefreshTokenHash refreshToken1 = new RefreshTokenHash(hash);
    RefreshTokenHash refreshToken2 = new RefreshTokenHash(hash);
    assertThat(refreshToken1).isEqualTo(refreshToken2);
    assertThat(refreshToken1.hashCode()).isEqualTo(refreshToken2.hashCode());
  }

  @ParameterizedTest
  @ValueSource(strings = { "hash1:hash2", "hashA:hashB" })
  void shouldNotBeEqual_WhenDifferentHash(String hashes) {
    String[] parts = hashes.split(":");
    RefreshTokenHash refreshToken1 = new RefreshTokenHash(parts[0]);
    RefreshTokenHash refreshToken2 = new RefreshTokenHash(parts[1]);
    assertThat(refreshToken1).isNotEqualTo(refreshToken2);
  }
}
