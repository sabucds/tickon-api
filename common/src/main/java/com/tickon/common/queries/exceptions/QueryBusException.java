package com.tickon.common.queries.exceptions;

public abstract class QueryBusException extends RuntimeException {
  protected QueryBusException(String message) {
    super(message);
  }

  protected QueryBusException(String message, Throwable cause) {
    super(message, cause);
  }
}
