package com.tickon.common.queries;

public interface Query<R> {
  default String getQueryName() {
    return this.getClass().getSimpleName();
  }
}
