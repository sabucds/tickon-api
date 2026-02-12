package com.tickon.common.queries;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface QueryResult<T> {

  record Success<T>(T value) implements QueryResult<T> {}

  default boolean isSuccess() {
    return this instanceof Success;
  }

  default Optional<T> toOptional() {
    if (this instanceof Success<T> success) {
      return Optional.ofNullable(success.value());
    }
    throw new IllegalStateException("Unexpected QueryResult type");
  }

  default <R> QueryResult<R> map(Function<T, R> mapper) {
    if (this instanceof Success<T> success) {
      return new Success<>(mapper.apply(success.value()));
    }
    throw new IllegalStateException("Unexpected QueryResult type");
  }

  default T orElse(T defaultValue) {
    if (this instanceof Success<T> success) {
      return success.value();
    }
    throw new IllegalStateException("Unexpected QueryResult type");
  }

  default T orElseThrow() {
    if (this instanceof Success<T> success) {
      return success.value();
    }
    throw new IllegalStateException("Unexpected QueryResult type");
  }

  default <X extends Throwable> T orElseThrow(Function<QueryResult<T>, X> exceptionSupplier) throws X {
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
}
