package com.tickon.common.queries;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface QueryResult<T> {

  record Success<T>(T value) implements QueryResult<T> {}

  record NotFound<T>() implements QueryResult<T> {}

  record Error<T>(String message, Throwable cause) implements QueryResult<T> {}

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default boolean isNotFound() {
    return this instanceof NotFound;
  }

  default boolean isError() {
    return this instanceof Error;
  }

  default Optional<T> toOptional() {
    return switch (this) {
    case Success<T> success -> Optional.of(success.value());
    default -> Optional.empty();
    };
  }

  default <R> QueryResult<R> map(Function<T, R> mapper) {
    return switch (this) {
    case Success<T> success -> new Success<>(mapper.apply(success.value()));
    case NotFound<T> ignored -> new NotFound<>();
    case Error<T> error -> new Error<>(error.message(), error.cause());
    };
  }

  default T orElse(T defaultValue) {
    return switch (this) {
    case Success<T> success -> success.value();
    default -> defaultValue;
    };
  }

  default T orElseThrow() {
    return switch (this) {
    case Success<T> success -> success.value();
    case NotFound<T> ignored -> throw new IllegalStateException("Query result is NotFound");
    case Error<T> error -> throw new IllegalStateException("Query result is Error: " + error.message(), error.cause());
    };
  }

  default <X extends Throwable> T orElseThrow(Function<QueryResult<T>, X> exceptionSupplier) throws X {
    return switch (this) {
    case Success<T> success -> success.value();
    default -> throw exceptionSupplier.apply(this);
    };
  }

  default void ifSuccess(Consumer<T> action) {
    if (this instanceof Success<T> success) {
      action.accept(success.value());
    }
  }

  default void ifNotFound(Runnable action) {
    if (this instanceof NotFound) {
      action.run();
    }
  }

  default void ifError(Consumer<Error<T>> action) {
    if (this instanceof Error<T> error) {
      action.accept(error);
    }
  }
}
