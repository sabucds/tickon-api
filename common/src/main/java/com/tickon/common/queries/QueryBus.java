package com.tickon.common.queries;

import com.tickon.common.queries.exceptions.QueryHandlerNotFoundException;

/**
 * Query bus for synchronous cross-module queries. Allows modules to query data
 * from other modules without direct dependencies. Follows the CQRS pattern
 * where queries are separated from commands.
 *
 * <p>
 * Usage example:
 * 
 * <pre>
 * QueryResult&lt;UserDTO&gt; result = queryBus.execute(new GetUserQuery(userId));
 * result.ifSuccess(user -&gt; System.out.println(user));
 * </pre>
 */
public interface QueryBus {
  /**
   * Execute a query and wait for the result. This is a synchronous operation.
   *
   * @param query the query to execute
   * @param <R>   the expected response type
   * @return the query result wrapped in QueryResult
   * @throws QueryHandlerNotFoundException if no handler is registered for this
   *                                       query
   */
  <R> QueryResult<R> execute(Query<R> query);
}
