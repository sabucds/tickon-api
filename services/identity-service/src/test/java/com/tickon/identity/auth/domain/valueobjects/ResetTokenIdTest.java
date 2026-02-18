package com.tickon.identity.auth.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResetTokenIdTest {

  @Test
  void shouldGenerateUniqueId() {
    ResetTokenId id1 = ResetTokenId.generate();
    ResetTokenId id2 = ResetTokenId.generate();

    assertThat(id1).isNotNull();
    assertThat(id2).isNotNull();
    assertThat(id1).isNotEqualTo(id2);
    assertThat(id1.value()).isNotEqualTo(id2.value());
  }

  @Test
  void shouldCreateFromUUID() {
    UUID uuid = UUID.randomUUID();
    ResetTokenId id = ResetTokenId.from(uuid);

    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void shouldThrowWhenUUIDIsNull() {
    assertThatThrownBy(() -> ResetTokenId.from(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("id");
  }

  @Test
  void shouldBeEqual_WhenSameUUID() {
    UUID uuid = UUID.randomUUID();
    ResetTokenId id1 = ResetTokenId.from(uuid);
    ResetTokenId id2 = ResetTokenId.from(uuid);

    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  void shouldNotBeEqual_WhenDifferentUUID() {
    ResetTokenId id1 = ResetTokenId.from(UUID.randomUUID());
    ResetTokenId id2 = ResetTokenId.from(UUID.randomUUID());

    assertThat(id1).isNotEqualTo(id2);
  }
}
