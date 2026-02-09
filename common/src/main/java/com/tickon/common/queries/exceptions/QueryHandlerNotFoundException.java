package com.tickon.common.queries.exceptions;

/**
 * Exception thrown when no handler is registered for a query. This typically
 * indicates a configuration issue where a query is being executed but no
 * corresponding handler bean was registered.
 */
public class QueryHandlerNotFoundException extends QueryBusException {
  private final Class<?> queryClass;

  public QueryHandlerNotFoundException(Class<?> queryClass) {
    super("No handler registered for query: " + queryClass.getName());
    this.queryClass = queryClass;
  }

  /**
   * Gets the query class for which no handler was found.
   *
   * @return the query class
   */
  public Class<?> getQueryClass() {
    return queryClass;
  }
}
