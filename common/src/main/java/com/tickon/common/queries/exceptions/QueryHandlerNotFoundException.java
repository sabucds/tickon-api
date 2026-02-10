package com.tickon.common.queries.exceptions;

public class QueryHandlerNotFoundException extends QueryBusException {
  private final Class<?> queryClass;

  public QueryHandlerNotFoundException(Class<?> queryClass) {
    super("No handler registered for query: " + queryClass.getName());
    this.queryClass = queryClass;
  }

  public Class<?> getQueryClass() {
    return queryClass;
  }
}
