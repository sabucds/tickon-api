package com.tickon.identity.auth.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResetTokenHashTest {

  @Test
  void shouldCreateFromValidString() {
    String hash = "abc123def456";
    ResetTokenHash tokenHash = ResetTokenHash.from(hash);

    assertThat(tokenHash.value()).isEqualTo(hash);
  }

  @Test
  void shouldThrowWhenValueIsNull() {
    assertThatThrownBy(() -> new ResetTokenHash(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("token hash");
  }

  @Test
  void shouldThrowWhenValueIsEmpty() {
    assertThatThrownBy(() -> new ResetTokenHash("")).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("token hash");
  }

  @Test
  void shouldThrowWhenValueIsBlank() {
    assertThatThrownBy(() -> new ResetTokenHash("   ")).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("token hash");
  }

  @Test
  void shouldBeEqual_WhenSameValue() {
    String hash = "abc123def456";
    ResetTokenHash hash1 = ResetTokenHash.from(hash);
    ResetTokenHash hash2 = ResetTokenHash.from(hash);

    assertThat(hash1).isEqualTo(hash2);
    assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
  }

  @Test
  void shouldNotBeEqual_WhenDifferentValue() {
    ResetTokenHash hash1 = ResetTokenHash.from("hash1");
    ResetTokenHash hash2 = ResetTokenHash.from("hash2");

    assertThat(hash1).isNotEqualTo(hash2);
  }
}
