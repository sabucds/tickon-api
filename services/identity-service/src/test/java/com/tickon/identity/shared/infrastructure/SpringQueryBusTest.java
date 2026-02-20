package com.tickon.identity.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.queries.Query;
import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.common.queries.exceptions.QueryExecutionException;
import com.tickon.common.queries.exceptions.QueryHandlerNotFoundException;
import com.tickon.identity.shared.platform.bus.SpringQueryBus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SpringQueryBusTest {

  @Test
  void shouldExecuteQuery_WhenHandlerExists() {

    TestQuery query = new TestQuery("test-param");
    TestQueryHandler handler = new TestQueryHandler("test-result");
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    QueryResult<Optional<String>> result = queryBus.execute(query);

    assertThat(result).isInstanceOf(QueryResult.Success.class);
    assertThat(result.orElseThrow()).hasValue("test-result");
  }

  @Test
  void shouldReturnEmptyOptional_WhenHandlerReturnsEmpty() {

    TestQuery query = new TestQuery("test-param");
    TestQueryHandler handler = new TestQueryHandler((String) null);
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    QueryResult<Optional<String>> result = queryBus.execute(query);

    assertThat(result).isInstanceOf(QueryResult.Success.class);
    assertThat(result.orElseThrow()).isEmpty();
  }

  @Test
  void shouldThrowException_WhenNoHandlerRegistered() {

    TestQuery query = new TestQuery("test-param");
    SpringQueryBus queryBus = new SpringQueryBus(List.of());

    assertThatThrownBy(() -> queryBus.execute(query)).isInstanceOf(QueryHandlerNotFoundException.class)
        .hasMessageContaining("No handler registered for query").hasMessageContaining("TestQuery");
  }

  @Test
  void shouldThrowException_WhenDuplicateHandlersRegistered() {

    TestQueryHandler handler1 = new TestQueryHandler("result1");
    TestQueryHandler handler2 = new TestQueryHandler("result2");

    assertThatThrownBy(() -> new SpringQueryBus(List.of(handler1, handler2))).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Multiple handlers registered").hasMessageContaining("TestQuery")
        .hasMessageContaining("TestQueryHandler");
  }

  @Test
  void shouldRegisterMultipleHandlers_ForDifferentQueries() {

    TestQueryHandler handler1 = new TestQueryHandler("result1");
    AnotherTestQueryHandler handler2 = new AnotherTestQueryHandler(42);
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler1, handler2));

    QueryResult<Optional<String>> result1 = queryBus.execute(new TestQuery("param"));
    QueryResult<Optional<Integer>> result2 = queryBus.execute(new AnotherTestQuery("param"));

    assertThat(queryBus.getHandlerCount()).isEqualTo(2);
    assertThat(result1.orElseThrow()).hasValue("result1");
    assertThat(result2.orElseThrow()).hasValue(42);
  }

  @Test
  void shouldWrapUnexpectedExceptions_InQueryExecutionException() {

    TestQuery query = new TestQuery("test-param");
    FailingQueryHandler handler = new FailingQueryHandler();
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    assertThatThrownBy(() -> queryBus.execute(query)).isInstanceOf(QueryExecutionException.class)
        .hasMessageContaining("Query execution failed").hasCauseInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldUseCustomQueryName_WhenProvided() {

    CustomNameQuery query = new CustomNameQuery();
    CustomNameQueryHandler handler = new CustomNameQueryHandler();
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    QueryResult<Optional<String>> result = queryBus.execute(query);

    assertThat(result.orElseThrow()).hasValue("custom");
    assertThat(query.getQueryName()).isEqualTo("CustomQuery.v1");
  }

  record TestQuery(String param) implements Query<Optional<String>> {}

  record AnotherTestQuery(String param) implements Query<Optional<Integer>> {}

  static class CustomNameQuery implements Query<Optional<String>> {
    @Override
    public String getQueryName() {
      return "CustomQuery.v1";
    }
  }

  static class TestQueryHandler implements QueryHandler<TestQuery, Optional<String>> {
    private final String result;

    TestQueryHandler(String result) {
      this.result = result;
    }

    @Override
    public QueryResult<Optional<String>> handle(TestQuery query) {
      return new QueryResult.Success<>(Optional.ofNullable(result));
    }

    @Override
    public Class<TestQuery> getQueryClass() {
      return TestQuery.class;
    }
  }

  static class AnotherTestQueryHandler implements QueryHandler<AnotherTestQuery, Optional<Integer>> {
    private final Integer result;

    AnotherTestQueryHandler(Integer result) {
      this.result = result;
    }

    @Override
    public QueryResult<Optional<Integer>> handle(AnotherTestQuery query) {
      return new QueryResult.Success<>(Optional.ofNullable(result));
    }

    @Override
    public Class<AnotherTestQuery> getQueryClass() {
      return AnotherTestQuery.class;
    }
  }

  static class CustomNameQueryHandler implements QueryHandler<CustomNameQuery, Optional<String>> {
    @Override
    public QueryResult<Optional<String>> handle(CustomNameQuery query) {
      return new QueryResult.Success<>(Optional.of("custom"));
    }

    @Override
    public Class<CustomNameQuery> getQueryClass() {
      return CustomNameQuery.class;
    }
  }

  static class FailingQueryHandler implements QueryHandler<TestQuery, Optional<String>> {
    @Override
    public QueryResult<Optional<String>> handle(TestQuery query) {
      throw new RuntimeException("Unexpected handler failure");
    }

    @Override
    public Class<TestQuery> getQueryClass() {
      return TestQuery.class;
    }
  }
}
