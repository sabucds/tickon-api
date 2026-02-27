package com.tickon.identity.shared.platform.bus;

import com.tickon.common.commands.Command;
import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandResult;
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
public class ObservableCommandBus implements CommandBus {

  private static final Logger log = LoggerFactory.getLogger(ObservableCommandBus.class);
  private static final String TIMER_NAME = "platform.command.duration";

  private final CommandBus delegate;
  private final MeterRegistry registry;

  public ObservableCommandBus(@Qualifier("springCommandBus") CommandBus delegate, MeterRegistry registry) {
    this.delegate = delegate;
    this.registry = registry;
  }

  @Override
  public <R> CommandResult<R> execute(Command<R> command) {
    if (command == null) {
      throw new IllegalArgumentException("Command must not be null");
    }
    String name = command.getCommandName();
    log.info("Dispatching {}", name);
    long startNano = System.nanoTime();
    try {
      CommandResult<R> result = delegate.execute(command);
      long ms = elapsedMs(startNano);
      log.info("{} succeeded ({}ms)", name, ms);
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

  private void recordTimer(String command, String outcome, long ms) {
    Timer.builder(TIMER_NAME).tag("command", command).tag("outcome", outcome).register(registry).record(ms,
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
