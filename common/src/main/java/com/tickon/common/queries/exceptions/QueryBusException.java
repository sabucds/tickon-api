package com.tickon.common.queries.exceptions;

/**
 * Base exception for query bus errors. All query bus related exceptions should
 * extend this class.
 */
public abstract class QueryBusException extends RuntimeException {
  protected QueryBusException(String message) {
    super(message);
  }

  protected QueryBusException(String message, Throwable cause) {
    super(message, cause);
  }
}
