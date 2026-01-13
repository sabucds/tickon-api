package com.tickon.identity.user.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserIdTest {

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794", "00000000-0000-0000-0000-000000000001" })
  void shouldCreateUserId_WhenValid(String validUserId) {
    UserId userId = UserId.from(validUserId);
    assertThat(userId.value().toString()).isEqualTo(validUserId);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "notAnUuid", "", "1234", "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz" })
  void shouldThrowException_WhenInvalidUserId(String invalidUserId) {
    assertThatThrownBy(() -> UserId.from(invalidUserId)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2 })
  void shouldGenerateUniqueIds(int run) {
    UserId generatedId1 = UserId.generate();
    UserId generatedId2 = UserId.generate();
    assertThat(generatedId1).isNotEqualTo(generatedId2);
    assertThat(generatedId1.value()).isNotNull();
    assertThat(generatedId2.value()).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794" })
  void shouldBeEqual_WhenSameId(String id) {
    UserId userId1 = UserId.from(id);
    UserId userId2 = UserId.from(id);
    assertThat(userId1).isEqualTo(userId2);
    assertThat(userId1.hashCode()).isEqualTo(userId2.hashCode());
  }
}
