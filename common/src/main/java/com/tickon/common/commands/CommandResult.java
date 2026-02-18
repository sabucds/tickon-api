package com.tickon.common.commands;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface CommandResult<T> {
  record Success<T>(T value) implements CommandResult<T> {}

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default Optional<T> toOptional() {
    if (this instanceof Success<T> success) {
      return Optional.ofNullable(success.value());
    }
    throw new IllegalStateException("Unexpected CommandResult type");
  }

  default T orElse(T defaultValue) {
    if (this instanceof Success<T> success) {
      return success.value();
    }
    throw new IllegalStateException("Unexpected CommandResult type");
  }

  default T orElseThrow() {
    if (this instanceof Success<T> success) {
      return success.value();
    }
    throw new IllegalStateException("Unexpected CommandResult type");
  }

  default <X extends Throwable> T orElseThrow(Function<CommandResult<T>, X> exceptionSupplier) throws X {
    if (this instanceof Success<T> success) {
      return success.value();
    }
    throw exceptionSupplier.apply(this);
  }

  default void ifSuccess(Consumer<T> action) {
    if (this instanceof Success<T> success) {
      action.accept(success.value());
    }
  }

  default <R> CommandResult<R> map(Function<T, R> mapper) {
    if (this instanceof Success<T> success) {
      return new Success<>(mapper.apply(success.value()));
    }
    throw new IllegalStateException("Unexpected CommandResult type");
  }
}
