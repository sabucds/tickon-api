package com.tickon.common.queries.exceptions;

/**
 * Exception thrown when a query handler fails during execution. This wraps any
 * unexpected exceptions that occur while processing a query.
 */
public class QueryExecutionException extends QueryBusException {
  private final Class<?> queryClass;

  public QueryExecutionException(Class<?> queryClass, String message, Throwable cause) {
    super("Query execution failed for " + queryClass.getName() + ": " + message, cause);
    this.queryClass = queryClass;
  }

  public QueryExecutionException(Class<?> queryClass, Throwable cause) {
    this(queryClass, cause.getMessage(), cause);
  }

  /**
   * Gets the query class that failed to execute.
   *
   * @return the query class
   */
  public Class<?> getQueryClass() {
    return queryClass;
  }
}
