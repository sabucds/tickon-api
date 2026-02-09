package com.tickon.identity.auth.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FamilyIdTest {

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794", "00000000-0000-0000-0000-000000000001" })
  void shouldCreateFamilyId_WhenValid(String validFamilyId) {
    FamilyId familyId = FamilyId.from(validFamilyId);
    assertThat(familyId.value().toString()).isEqualTo(validFamilyId);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "notAnUuid", "", "1234", "zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz" })
  void shouldThrowException_WhenInvalidFamilyId(String invalidFamilyId) {
    assertThatThrownBy(() -> FamilyId.from(invalidFamilyId)).isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2 })
  void shouldGenerateUniqueIds(int run) {
    FamilyId generatedId1 = FamilyId.generate();
    FamilyId generatedId2 = FamilyId.generate();
    assertThat(generatedId1).isNotEqualTo(generatedId2);
    assertThat(generatedId1.value()).isNotNull();
    assertThat(generatedId2.value()).isNotNull();
  }

  @ParameterizedTest
  @ValueSource(strings = { "b350b3cd-2fbd-4248-b35f-bd1917367794" })
  void shouldBeEqual_WhenSameId(String id) {
    FamilyId familyId1 = FamilyId.from(id);
    FamilyId familyId2 = FamilyId.from(id);
    assertThat(familyId1).isEqualTo(familyId2);
    assertThat(familyId1.hashCode()).isEqualTo(familyId2.hashCode());
  }

  @Test
  void shouldThrow_WhenPassingNullToConstructor() {
    assertThatThrownBy(() -> new FamilyId(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid FamilyId: null value");
  }
}
