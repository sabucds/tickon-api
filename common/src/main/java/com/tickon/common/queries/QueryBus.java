package com.tickon.common.queries;

public interface QueryBus {

  <R> QueryResult<R> execute(Query<R> query);
}
