package com.tickon.identity.shared.infrastructure;

import com.tickon.common.queries.Query;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.common.queries.exceptions.QueryExecutionException;
import com.tickon.common.queries.exceptions.QueryHandlerNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SpringQueryBus implements QueryBus {
  private static final Logger log = LoggerFactory.getLogger(SpringQueryBus.class);

  private final Map<Class<?>, QueryHandler<?, ?>> handlers = new HashMap<>();

  public SpringQueryBus(List<QueryHandler<?, ?>> handlers) {
    log.info("Initializing QueryBus with {} handler(s)", handlers.size());

    Map<Class<?>, List<QueryHandler<?, ?>>> grouped = handlers.stream()
        .collect(Collectors.groupingBy(QueryHandler::getQueryClass));

    grouped.forEach((queryClass, handlerList) -> {
      if (handlerList.size() > 1) {
        String handlerNames = handlerList.stream().map(h -> h.getClass().getSimpleName())
            .collect(Collectors.joining(", "));
        String errorMsg = String.format(
            "Multiple handlers registered for query %s: [%s]. Only one handler per query is allowed.",
            queryClass.getSimpleName(), handlerNames);
        log.error(errorMsg);
        throw new IllegalStateException(errorMsg);
      }

      QueryHandler<?, ?> handler = handlerList.get(0);
      this.handlers.put(queryClass, handler);
      log.debug("Registered query handler: {} for query: {}", handler.getClass().getSimpleName(),
          queryClass.getSimpleName());
    });

    log.info("QueryBus initialized successfully with {} unique handler(s)", this.handlers.size());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> QueryResult<R> execute(Query<R> query) {
    String queryName = query.getQueryName();
    log.debug("Executing query: {}", queryName);
    long startTime = System.currentTimeMillis();

    try {
      QueryHandler<Query<R>, R> handler = (QueryHandler<Query<R>, R>) handlers.get(query.getClass());

      if (handler == null) {
        log.error("No handler found for query: {}", query.getClass().getName());
        throw new QueryHandlerNotFoundException(query.getClass());
      }

      QueryResult<R> result = handler.handle(query);
      long duration = System.currentTimeMillis() - startTime;

      logQueryResult(queryName, result, duration);

      return result;
    } catch (QueryHandlerNotFoundException e) {

      throw e;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Query execution failed: {} ({}ms)", queryName, duration, e);
      throw new QueryExecutionException(query.getClass(), e);
    }
  }

  private void logQueryResult(String queryName, QueryResult<?> result, long duration) {
    if (result instanceof QueryResult.Success<?>) {
      log.debug("Query executed successfully: {} ({}ms)", queryName, duration);
    }
  }

  public int getHandlerCount() {
    return handlers.size();
  }
}
