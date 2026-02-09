package com.tickon.identity.auth.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SessionIdTest {

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794", "00000000-0000-0000-0000-000000000001" })
  void shouldCreateSessionId_WhenValid(String validSessionId) {
    SessionId sessionId = SessionId.from(validSessionId);
    assertThat(sessionId.value().toString()).isEqualTo(validSessionId);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "notAnUuid", "", "1234", "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz" })
  void shouldThrowException_WhenInvalidSessionId(String invalidSessionId) {
    assertThatThrownBy(() -> SessionId.from(invalidSessionId)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2 })
  void shouldGenerateUniqueIds(int run) {
    SessionId generatedId1 = SessionId.generate();
    SessionId generatedId2 = SessionId.generate();
    assertThat(generatedId1).isNotEqualTo(generatedId2);
    assertThat(generatedId1.value()).isNotNull();
    assertThat(generatedId2.value()).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794" })
  void shouldBeEqual_WhenSameId(String id) {
    SessionId sessionId1 = SessionId.from(id);
    SessionId sessionId2 = SessionId.from(id);
    assertThat(sessionId1).isEqualTo(sessionId2);
    assertThat(sessionId1.hashCode()).isEqualTo(sessionId2.hashCode());
  }

  @Test
  void shouldThrow_WhenPassingNullToConstructor() {
    assertThatThrownBy(() -> new SessionId(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid SessionId: null value");
  }
}
