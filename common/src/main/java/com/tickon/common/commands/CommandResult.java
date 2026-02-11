package com.tickon.common.commands;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface CommandResult<T> {
  record Success<T>(T value) implements CommandResult<T> {}

  record Error<T>(String message, Throwable cause) implements CommandResult<T> {}

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default boolean isError() {
    return this instanceof Error;
  }

  default Optional<T> toOptional() {
    return switch (this) {
    case Success<T> success -> Optional.ofNullable(success.value());
    case Error<T> ignored -> Optional.empty();
    };
  }

  default T orElse(T defaultValue) {
    return switch (this) {
    case Success<T> success -> success.value();
    case Error<T> ignored -> defaultValue;
    };
  }

  default T orElseThrow() {
    return switch (this) {
    case Success<T> success -> success.value();
    case Error<T> error ->
      throw new IllegalStateException("Command result is Error: " + error.message(), error.cause());
    };
  }

  default <X extends Throwable> T orElseThrow(Function<CommandResult<T>, X> exceptionSupplier) throws X {
    return switch (this) {
    case Success<T> success -> success.value();
    case Error<T> ignored -> throw exceptionSupplier.apply(this);
    };
  }

  default void ifSuccess(Consumer<T> action) {
    if (this instanceof Success<T> success) {
      action.accept(success.value());
    }
  }

  default void ifError(Consumer<Error<T>> action) {
    if (this instanceof Error<T> error) {
      action.accept(error);
    }
  }

  default <R> CommandResult<R> map(Function<T, R> mapper) {
    return switch (this) {
    case Success<T> success -> new Success<>(mapper.apply(success.value()));
    case Error<T> error -> new Error<>(error.message(), error.cause());
    };
  }
}
