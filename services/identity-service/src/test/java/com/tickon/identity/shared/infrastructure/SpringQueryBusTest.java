package com.tickon.identity.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.queries.Query;
import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.common.queries.exceptions.QueryExecutionException;
import com.tickon.common.queries.exceptions.QueryHandlerNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SpringQueryBus. Tests handler registration, query execution,
 * error handling, and validation.
 */
class SpringQueryBusTest {

  @Test
  void shouldExecuteQuery_WhenHandlerExists() {
    // Given
    TestQuery query = new TestQuery("test-param");
    TestQueryHandler handler = new TestQueryHandler("test-result");
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    // When
    QueryResult<String> result = queryBus.execute(query);

    // Then
    assertThat(result).isInstanceOf(QueryResult.Success.class);
    assertThat(result.toOptional()).hasValue("test-result");
  }

  @Test
  void shouldReturnNotFound_WhenHandlerReturnsNotFound() {
    // Given
    TestQuery query = new TestQuery("test-param");
    TestQueryHandler handler = new TestQueryHandler((String) null); // Returns NotFound
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    // When
    QueryResult<String> result = queryBus.execute(query);

    // Then
    assertThat(result).isInstanceOf(QueryResult.NotFound.class);
    assertThat(result.toOptional()).isEmpty();
  }

  @Test
  void shouldReturnError_WhenHandlerReturnsError() {
    // Given
    TestQuery query = new TestQuery("test-param");
    TestQueryHandler handler = new TestQueryHandler(new RuntimeException("test-error"));
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    // When
    QueryResult<String> result = queryBus.execute(query);

    // Then
    assertThat(result).isInstanceOf(QueryResult.Error.class);
    assertThat(result.isError()).isTrue();
  }

  @Test
  void shouldThrowException_WhenNoHandlerRegistered() {
    // Given
    TestQuery query = new TestQuery("test-param");
    SpringQueryBus queryBus = new SpringQueryBus(List.of());

    // When & Then
    assertThatThrownBy(() -> queryBus.execute(query)).isInstanceOf(QueryHandlerNotFoundException.class)
        .hasMessageContaining("No handler registered for query").hasMessageContaining("TestQuery");
  }

  @Test
  void shouldThrowException_WhenDuplicateHandlersRegistered() {
    // Given
    TestQueryHandler handler1 = new TestQueryHandler("result1");
    TestQueryHandler handler2 = new TestQueryHandler("result2");

    // When & Then
    assertThatThrownBy(() -> new SpringQueryBus(List.of(handler1, handler2))).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Multiple handlers registered").hasMessageContaining("TestQuery")
        .hasMessageContaining("TestQueryHandler");
  }

  @Test
  void shouldRegisterMultipleHandlers_ForDifferentQueries() {
    // Given
    TestQueryHandler handler1 = new TestQueryHandler("result1");
    AnotherTestQueryHandler handler2 = new AnotherTestQueryHandler(42);
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler1, handler2));

    // When
    QueryResult<String> result1 = queryBus.execute(new TestQuery("param"));
    QueryResult<Integer> result2 = queryBus.execute(new AnotherTestQuery("param"));

    // Then
    assertThat(queryBus.getHandlerCount()).isEqualTo(2);
    assertThat(result1.toOptional()).hasValue("result1");
    assertThat(result2.toOptional()).hasValue(42);
  }

  @Test
  void shouldWrapUnexpectedExceptions_InQueryExecutionException() {
    // Given
    TestQuery query = new TestQuery("test-param");
    FailingQueryHandler handler = new FailingQueryHandler();
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    // When & Then
    assertThatThrownBy(() -> queryBus.execute(query)).isInstanceOf(QueryExecutionException.class)
        .hasMessageContaining("Query execution failed").hasCauseInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldUseCustomQueryName_WhenProvided() {
    // Given
    CustomNameQuery query = new CustomNameQuery();
    CustomNameQueryHandler handler = new CustomNameQueryHandler();
    SpringQueryBus queryBus = new SpringQueryBus(List.of(handler));

    // When
    QueryResult<String> result = queryBus.execute(query);

    // Then
    assertThat(result.toOptional()).hasValue("custom");
    assertThat(query.getQueryName()).isEqualTo("CustomQuery.v1");
  }

  // Test fixtures

  record TestQuery(String param) implements Query<String> {}

  record AnotherTestQuery(String param) implements Query<Integer> {}

  static class CustomNameQuery implements Query<String> {
    @Override
    public String getQueryName() {
      return "CustomQuery.v1";
    }
  }

  static class TestQueryHandler implements QueryHandler<TestQuery, String> {
    private final String result;
    private final RuntimeException error;

    TestQueryHandler(String result) {
      this.result = result;
      this.error = null;
    }

    TestQueryHandler(RuntimeException error) {
      this.result = null;
      this.error = error;
    }

    @Override
    public QueryResult<String> handle(TestQuery query) {
      if (error != null) {
        return new QueryResult.Error<>("Handler error", error);
      }
      if (result == null) {
        return new QueryResult.NotFound<>();
      }
      return new QueryResult.Success<>(result);
    }

    @Override
    public Class<TestQuery> getQueryClass() {
      return TestQuery.class;
    }
  }

  static class AnotherTestQueryHandler implements QueryHandler<AnotherTestQuery, Integer> {
    private final Integer result;

    AnotherTestQueryHandler(Integer result) {
      this.result = result;
    }

    @Override
    public QueryResult<Integer> handle(AnotherTestQuery query) {
      return new QueryResult.Success<>(result);
    }

    @Override
    public Class<AnotherTestQuery> getQueryClass() {
      return AnotherTestQuery.class;
    }
  }

  static class CustomNameQueryHandler implements QueryHandler<CustomNameQuery, String> {
    @Override
    public QueryResult<String> handle(CustomNameQuery query) {
      return new QueryResult.Success<>("custom");
    }

    @Override
    public Class<CustomNameQuery> getQueryClass() {
      return CustomNameQuery.class;
    }
  }

  static class FailingQueryHandler implements QueryHandler<TestQuery, String> {
    @Override
    public QueryResult<String> handle(TestQuery query) {
      throw new RuntimeException("Unexpected handler failure");
    }

    @Override
    public Class<TestQuery> getQueryClass() {
      return TestQuery.class;
    }
  }
}
