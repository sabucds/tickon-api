package com.tickon.common.queries;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Wrapper for query results with explicit error handling. Allows handlers to
 * communicate success, not found, or error conditions explicitly.
 *
 * @param <T> the result type
 */
public sealed interface QueryResult<T> {
  /**
   * Successful query result with a value.
   */
  record Success<T>(T value) implements QueryResult<T> {}

  /**
   * Query executed successfully but no data was found.
   */
  record NotFound<T>() implements QueryResult<T> {}

  /**
   * Query execution failed with an error.
   */
  record Error<T>(String message, Throwable cause) implements QueryResult<T> {}

  /**
   * Checks if the result is successful.
   *
   * @return true if this is a Success result
   */
  default boolean isSuccess() {
    return this instanceof Success;
  }

  /**
   * Checks if the result is not found.
   *
   * @return true if this is a NotFound result
   */
  default boolean isNotFound() {
    return this instanceof NotFound;
  }

  /**
   * Checks if the result is an error.
   *
   * @return true if this is an Error result
   */
  default boolean isError() {
    return this instanceof Error;
  }

  /**
   * Converts this result to an Optional. Returns Optional.of(value) for Success,
   * Optional.empty() otherwise.
   *
   * @return the optional value
   */
  default Optional<T> toOptional() {
    return switch (this) {
    case Success<T> success -> Optional.of(success.value());
    default -> Optional.empty();
    };
  }

  /**
   * Maps the success value to another type. Preserves NotFound and Error states.
   *
   * @param mapper the mapping function
   * @param <R>    the new result type
   * @return the mapped result
   */
  default <R> QueryResult<R> map(Function<T, R> mapper) {
    return switch (this) {
    case Success<T> success -> new Success<>(mapper.apply(success.value()));
    case NotFound<T> ignored -> new NotFound<>();
    case Error<T> error -> new Error<>(error.message(), error.cause());
    };
  }

  /**
   * Returns the value if successful, otherwise returns the provided default.
   *
   * @param defaultValue the default value to return if not successful
   * @return the value or default
   */
  default T orElse(T defaultValue) {
    return switch (this) {
    case Success<T> success -> success.value();
    default -> defaultValue;
    };
  }

  /**
   * Returns the value if successful, otherwise throws an exception.
   *
   * @return the value
   * @throws IllegalStateException if not successful
   */
  default T orElseThrow() {
    return switch (this) {
    case Success<T> success -> success.value();
    case NotFound<T> ignored -> throw new IllegalStateException("Query result is NotFound");
    case Error<T> error -> throw new IllegalStateException("Query result is Error: " + error.message(), error.cause());
    };
  }

  /**
   * Returns the value if successful, otherwise throws a custom exception.
   *
   * @param exceptionSupplier supplier for the exception to throw
   * @param <X>               the exception type
   * @return the value
   * @throws X if not successful
   */
  default <X extends Throwable> T orElseThrow(Function<QueryResult<T>, X> exceptionSupplier) throws X {
    return switch (this) {
    case Success<T> success -> success.value();
    default -> throw exceptionSupplier.apply(this);
    };
  }

  /**
   * Executes the provided action if the result is successful.
   *
   * @param action the action to execute
   */
  default void ifSuccess(Consumer<T> action) {
    if (this instanceof Success<T> success) {
      action.accept(success.value());
    }
  }

  /**
   * Executes the provided action if the result is not found.
   *
   * @param action the action to execute
   */
  default void ifNotFound(Runnable action) {
    if (this instanceof NotFound) {
      action.run();
    }
  }

  /**
   * Executes the provided action if the result is an error.
   *
   * @param action the action to execute
   */
  default void ifError(Consumer<Error<T>> action) {
    if (this instanceof Error<T> error) {
      action.accept(error);
    }
  }
}
