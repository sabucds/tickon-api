package com.tickon.common.queries;

public interface QueryHandler<Q extends Query<R>, R> {

  QueryResult<R> handle(Q query);

  Class<Q> getQueryClass();
}
