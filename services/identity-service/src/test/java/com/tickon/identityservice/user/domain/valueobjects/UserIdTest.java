package com.tickon.identityservice.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UserIdTest {
  @Test
  void shouldCreateUserId_WhenValid() {
    String validUserId = "b350b3cd-2fbd-4248-b35f-bd1917367794";
    UserId userId = UserId.from(validUserId);
    assertThat(userId.toString()).hasToString(validUserId);
  }

  @Test
  void shouldThrowException_WhenInvalidUserId() {
    String invalidUserId = "notAnUuid";
    assertThatThrownBy(() -> UserId.from(invalidUserId)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldGenerateARandomId() {
    UserId generatedId = UserId.generate();
    assertThat(generatedId.toString()).isNotNull();
  }

  @Test
  void shouldReturnIdHashCode() {
    UserId userId1 = UserId.generate();
    UserId userId2 = UserId.generate();
    assertThat(userId1.hashCode()).isNotEqualTo(userId2.hashCode());
  }

  @Test
  void shouldBeEqual_WhenSameId() {
    String id = "b350b3cd-2fbd-4248-b35f-bd1917367794";
    UserId userId1 = UserId.from(id);
    UserId userId2 = UserId.from(id);
    assertThat(userId1).isEqualTo(userId2);
    assertThat(userId1.hashCode()).hasSameHashCodeAs(userId2.hashCode());
  }
}
