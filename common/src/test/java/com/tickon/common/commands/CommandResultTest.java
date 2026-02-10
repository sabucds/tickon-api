package com.tickon.common.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class CommandResultTest {

  @Test
  void shouldCreateSuccessResult() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.isError()).isFalse();
  }

  @Test
  void shouldCreateErrorResult() {
    Exception cause = new RuntimeException("test error");
    CommandResult<String> result = new CommandResult.Error<>("error message", cause);

    assertThat(result.isSuccess()).isFalse();
    assertThat(result.isError()).isTrue();
  }

  @Test
  void shouldConvertSuccessToOptional() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    Optional<String> optional = result.toOptional();

    assertThat(optional).isPresent();
    assertThat(optional.get()).isEqualTo("test-value");
  }

  @Test
  void shouldConvertErrorToEmptyOptional() {
    CommandResult<String> result = new CommandResult.Error<>("error", new RuntimeException());

    Optional<String> optional = result.toOptional();

    assertThat(optional).isEmpty();
  }

  @Test
  void shouldReturnValueWithOrElse_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("actual-value");

    String value = result.orElse("default-value");

    assertThat(value).isEqualTo("actual-value");
  }

  @Test
  void shouldReturnDefaultWithOrElse_WhenError() {
    CommandResult<String> result = new CommandResult.Error<>("error", new RuntimeException());

    String value = result.orElse("default-value");

    assertThat(value).isEqualTo("default-value");
  }

  @Test
  void shouldReturnValueWithOrElseThrow_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    String value = result.orElseThrow();

    assertThat(value).isEqualTo("test-value");
  }

  @Test
  void shouldThrowWithOrElseThrow_WhenError() {
    CommandResult<String> result = new CommandResult.Error<>("error message", new RuntimeException("cause"));

    assertThatThrownBy(result::orElseThrow).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("error message").hasCauseInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldReturnValueWithOrElseThrowWithSupplier_WhenSuccess() throws Exception {
    CommandResult<String> result = new CommandResult.Success<>("test-value");

    String value = result.orElseThrow(r -> new Exception("should not be thrown"));

    assertThat(value).isEqualTo("test-value");
  }

  @Test
  void shouldThrowWithOrElseThrowWithSupplier_WhenError() {
    CommandResult<String> result = new CommandResult.Error<>("error", new RuntimeException());

    assertThatThrownBy(() -> result.orElseThrow(r -> new IllegalArgumentException("custom error")))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("custom error");
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
  void shouldNotExecuteIfSuccess_WhenError() {
    CommandResult<String> result = new CommandResult.Error<>("error", new RuntimeException());
    AtomicBoolean executed = new AtomicBoolean(false);

    result.ifSuccess(value -> executed.set(true));

    assertThat(executed.get()).isFalse();
  }

  @Test
  void shouldExecuteIfError_WhenError() {
    RuntimeException cause = new RuntimeException("cause");
    CommandResult<String> result = new CommandResult.Error<>("error message", cause);
    AtomicBoolean executed = new AtomicBoolean(false);

    result.ifError(error -> {
      assertThat(error.message()).isEqualTo("error message");
      assertThat(error.cause()).isEqualTo(cause);
      executed.set(true);
    });

    assertThat(executed.get()).isTrue();
  }

  @Test
  void shouldNotExecuteIfError_WhenSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test-value");
    AtomicBoolean executed = new AtomicBoolean(false);

    result.ifError(error -> executed.set(true));

    assertThat(executed.get()).isFalse();
  }

  @Test
  void shouldMapSuccess() {
    CommandResult<String> result = new CommandResult.Success<>("test");

    CommandResult<Integer> mapped = result.map(String::length);

    assertThat(mapped.isSuccess()).isTrue();
    assertThat(mapped.orElseThrow()).isEqualTo(4);
  }

  @Test
  void shouldMapError() {
    RuntimeException cause = new RuntimeException();
    CommandResult<String> result = new CommandResult.Error<>("error message", cause);

    CommandResult<Integer> mapped = result.map(String::length);

    assertThat(mapped.isError()).isTrue();
    assertThat(mapped).isInstanceOf(CommandResult.Error.class);
    CommandResult.Error<Integer> error = (CommandResult.Error<Integer>) mapped;
    assertThat(error.message()).isEqualTo("error message");
    assertThat(error.cause()).isEqualTo(cause);
  }

  @Test
  void shouldHandleNullValueInSuccess() {
    CommandResult<String> result = new CommandResult.Success<>(null);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.orElse("default")).isNull();
  }
}
