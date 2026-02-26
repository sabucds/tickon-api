package com.tickon.identity.shared.platform.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tickon.common.queries.Query;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.common.queries.exceptions.QueryExecutionException;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObservableQueryBusTest {

  record TestQuery(String value) implements Query<String> {}

  private SimpleMeterRegistry registry;
  private QueryBus delegate;
  private ObservableQueryBus bus;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    delegate = mock(QueryBus.class);
    bus = new ObservableQueryBus(delegate, registry);
  }

  @Test
  void should_ReturnResult_When_QuerySucceeds() {
    TestQuery query = new TestQuery("x");
    QueryResult<String> expected = new QueryResult.Success<>("ok");
    when(delegate.execute(query)).thenReturn(expected);

    QueryResult<String> result = bus.execute(query);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void should_RecordSuccessTimer_When_QuerySucceeds() {
    TestQuery query = new TestQuery("x");
    when(delegate.execute(query)).thenReturn(new QueryResult.Success<>("ok"));

    bus.execute(query);

    Timer timer = registry.find("platform.query.duration")
        .tag("query", "TestQuery")
        .tag("outcome", "success")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
  }

  @Test
  void should_RecordFailureTimer_When_QueryThrows() {
    TestQuery query = new TestQuery("x");
    when(delegate.execute(query))
        .thenThrow(new QueryExecutionException(TestQuery.class, new RuntimeException("boom")));

    assertThatThrownBy(() -> bus.execute(query)).isInstanceOf(RuntimeException.class);

    Timer timer = registry.find("platform.query.duration")
        .tag("query", "TestQuery")
        .tag("outcome", "failure")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
  }

  @Test
  void should_RethrowOriginalException_When_DelegateThrows() {
    TestQuery query = new TestQuery("x");
    RuntimeException ex = new QueryExecutionException(TestQuery.class, new RuntimeException("original"));
    when(delegate.execute(query)).thenThrow(ex);

    assertThatThrownBy(() -> bus.execute(query)).isSameAs(ex);
  }
}
