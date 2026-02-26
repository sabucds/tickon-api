package com.tickon.identity.shared.platform.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tickon.common.commands.Command;
import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.commands.exceptions.CommandExecutionException;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObservableCommandBusTest {

  record TestCommand(String value) implements Command<String> {}

  private SimpleMeterRegistry registry;
  private CommandBus delegate;
  private ObservableCommandBus bus;

  @BeforeEach
  void setUp() {
    registry = new SimpleMeterRegistry();
    delegate = mock(CommandBus.class);
    bus = new ObservableCommandBus(delegate, registry);
  }

  @Test
  void should_ReturnResult_When_CommandSucceeds() {
    TestCommand cmd = new TestCommand("x");
    CommandResult<String> expected = new CommandResult.Success<>("ok");
    when(delegate.execute(cmd)).thenReturn(expected);

    CommandResult<String> result = bus.execute(cmd);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void should_RecordSuccessTimer_When_CommandSucceeds() {
    TestCommand cmd = new TestCommand("x");
    when(delegate.execute(cmd)).thenReturn(new CommandResult.Success<>("ok"));

    bus.execute(cmd);

    Timer timer = registry.find("platform.command.duration")
        .tag("command", "TestCommand")
        .tag("outcome", "success")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
  }

  @Test
  void should_RecordFailureTimer_When_CommandThrows() {
    TestCommand cmd = new TestCommand("x");
    when(delegate.execute(cmd))
        .thenThrow(new CommandExecutionException(TestCommand.class, new RuntimeException("boom")));

    assertThatThrownBy(() -> bus.execute(cmd)).isInstanceOf(RuntimeException.class);

    Timer timer = registry.find("platform.command.duration")
        .tag("command", "TestCommand")
        .tag("outcome", "failure")
        .timer();
    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
  }

  @Test
  void should_RethrowOriginalException_When_DelegateThrows() {
    TestCommand cmd = new TestCommand("x");
    RuntimeException ex = new CommandExecutionException(TestCommand.class, new RuntimeException("original"));
    when(delegate.execute(cmd)).thenThrow(ex);

    assertThatThrownBy(() -> bus.execute(cmd)).isSameAs(ex);
  }

  @Test
  void should_ThrowIllegalArgumentException_When_CommandIsNull() {
    assertThatThrownBy(() -> bus.execute(null)).isInstanceOf(IllegalArgumentException.class);
  }
}
