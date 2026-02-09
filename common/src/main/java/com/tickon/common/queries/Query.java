package com.tickon.common.queries;

/**
 * Marker interface for queries in CQRS pattern. Each query represents a request
 * for data that can be handled by any module. Queries should be immutable and
 * contain only the parameters needed to fetch data.
 *
 * @param <R> the response type this query expects
 */
public interface Query<R> {
  /**
   * Returns a human-readable name for this query. Used for logging, monitoring,
   * and debugging purposes. Default implementation returns the simple class name.
   *
   * @return the query name
   */
  default String getQueryName() {
    return this.getClass().getSimpleName();
  }
}
