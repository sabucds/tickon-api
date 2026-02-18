package com.tickon.common.queries.exceptions;

public class QueryExecutionException extends QueryBusException {
  private final Class<?> queryClass;

  public QueryExecutionException(Class<?> queryClass, String message, Throwable cause) {
    super("Query execution failed for " + queryClass.getName() + ": " + message, cause);
    this.queryClass = queryClass;
  }

  public QueryExecutionException(Class<?> queryClass, Throwable cause) {
    this(queryClass, cause.getMessage(), cause);
  }

  public Class<?> getQueryClass() {
    return queryClass;
  }
}
