package com.tickon.common.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class CommandResultTest {

  @Test
  void shouldCreateSuccessResult() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    assertThat(result.isSuccess()).isTrue();
  }

  @Test
  void shouldConvertSuccessToOptional() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    Optional<String> optional = result.toOptional();

    assertThat(optional).isPresent();
    assertThat(optional.get()).isEqualTo("test-value");
  }

  @Test
  void shouldReturnValueWithOrElse_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("actual-value");

    String value = result.orElse("default-value");

    assertThat(value).isEqualTo("actual-value");
  }

  @Test
  void shouldReturnValueWithOrElseThrow_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    String value = result.orElseThrow();

    assertThat(value).isEqualTo("test-value");
  }

  @Test
  void shouldReturnValueWithOrElseThrowWithSupplier_WhenSuccess() throws Exception {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    String value = result.orElseThrow(r -> new Exception("should not be thrown"));

    assertThat(value).isEqualTo("test-value");
  }

  @Test
  void shouldExecuteIfSuccess_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");
    AtomicBoolean executed = new AtomicBoolean(false);

    result.ifSuccess(value -> {
      assertThat(value).isEqualTo("test-value");
      executed.set(true);
    });

    assertThat(executed.get()).isTrue();
  }

  @Test
  void shouldMapSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test");

    CommandResult<Integer> mapped = result.map(String::length);

    assertThat(mapped.isSuccess()).isTrue();
    assertThat(mapped.orElseThrow()).isEqualTo(4);
  }

  @Test
  void shouldHandleNullValueInSuccess() {
    CommandResult<String> result = new CommandResult.Success<>(null);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.orElse("default")).isNull();
  }

  @Test
  void shouldConvertSuccessWithNullToEmptyOptional() {
    CommandResult<String> result = new CommandResult.Success<>(null);

    Optional<String> optional = result.toOptional();

    assertThat(optional).isEmpty();
  }
}
