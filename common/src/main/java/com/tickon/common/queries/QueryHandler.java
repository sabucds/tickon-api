package com.tickon.common.queries;

/**
 * Handler for a specific query type. Each module registers handlers for queries
 * it can answer. Handlers should be stateless and registered as Spring beans.
 *
 * @param <Q> the query type this handler processes
 * @param <R> the response type
 */
public interface QueryHandler<Q extends Query<R>, R> {
  /**
   * Handle the query and return the result. Should wrap the result in a
   * QueryResult to communicate success, not found, or error states.
   *
   * @param query the query to handle
   * @return the query result wrapped in QueryResult
   */
  QueryResult<R> handle(Q query);

  /**
   * Get the query class this handler supports. Used for handler registration and
   * lookup in the query bus.
   *
   * @return the query class
   */
  Class<Q> getQueryClass();
}
