package com.tickon.identity.shared.platform.bus;

import com.tickon.common.queries.Query;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ObservableQueryBus implements QueryBus {

  private static final Logger log = LoggerFactory.getLogger(ObservableQueryBus.class);
  private static final String TIMER_NAME = "platform.query.duration";

  private final QueryBus delegate;
  private final MeterRegistry registry;

  public ObservableQueryBus(@Qualifier("springQueryBus") QueryBus delegate, MeterRegistry registry) {
    this.delegate = delegate;
    this.registry = registry;
  }

  @Override
  public <R> QueryResult<R> execute(Query<R> query) {
    String name = query.getQueryName();
    log.debug("Dispatching {}", name);
    long startNano = System.nanoTime();
    try {
      QueryResult<R> result = delegate.execute(query);
      long ms = elapsedMs(startNano);
      log.debug("{} succeeded ({}ms)", name, ms);
      recordTimer(name, "success", ms);
      return result;
    } catch (RuntimeException ex) {
      long ms = elapsedMs(startNano);
      if (isDomainException(ex)) {
        log.warn("{} failed ({}ms): {}", name, ms, rootCause(ex).getClass().getSimpleName());
      } else {
        log.error("{} failed unexpectedly ({}ms)", name, ms, ex);
      }
      recordTimer(name, "failure", ms);
      throw ex;
    }
  }

  private void recordTimer(String query, String outcome, long ms) {
    Timer.builder(TIMER_NAME).tag("query", query).tag("outcome", outcome).register(registry).record(ms,
        TimeUnit.MILLISECONDS);
  }

  private long elapsedMs(long startNano) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
  }

  private boolean isDomainException(RuntimeException ex) {
    return rootCause(ex).getClass().getPackageName().contains(".domain.");
  }

  private Throwable rootCause(Throwable ex) {
    return ex.getCause() != null ? ex.getCause() : ex;
  }
}
