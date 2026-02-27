package com.tickon.identity.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tickon.common.commands.Command;
import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.commands.exceptions.CommandExecutionException;
import com.tickon.common.commands.exceptions.CommandHandlerNotFoundException;
import com.tickon.identity.shared.platform.bus.SpringCommandBus;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpringCommandBusTest {

  // Test command
  record TestCommand(String value) implements Command<String> {}

  // Test command handler
  static class TestCommandHandler implements CommandHandler<TestCommand, String> {
    @Override
    public CommandResult<String> handle(TestCommand command) {
      return new CommandResult.Success<>("handled-" + command.value());
    }

    @Override
    public Class<TestCommand> getCommandClass() {
      return TestCommand.class;
    }
  }

  // Failing command handler
  static class FailingCommandHandler implements CommandHandler<TestCommand, String> {
    @Override
    public CommandResult<String> handle(TestCommand command) {
      throw new RuntimeException("Handler failure");
    }

    @Override
    public Class<TestCommand> getCommandClass() {
      return TestCommand.class;
    }
  }

  // Another test command
  record AnotherCommand(int value) implements Command<Integer> {}

  // Another command handler
  static class AnotherCommandHandler implements CommandHandler<AnotherCommand, Integer> {
    @Override
    public CommandResult<Integer> handle(AnotherCommand command) {
      return new CommandResult.Success<>(command.value() * 2);
    }

    @Override
    public Class<AnotherCommand> getCommandClass() {
      return AnotherCommand.class;
    }
  }

  @Test
  void shouldInitializeWithHandlers() {
    TestCommandHandler handler = new TestCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler));

    assertThat(commandBus.getHandlerCount()).isEqualTo(1);
  }

  @Test
  void shouldInitializeWithMultipleHandlers() {
    TestCommandHandler handler1 = new TestCommandHandler();
    AnotherCommandHandler handler2 = new AnotherCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler1, handler2));

    assertThat(commandBus.getHandlerCount()).isEqualTo(2);
  }

  @Test
  void shouldInitializeWithNoHandlers() {
    SpringCommandBus commandBus = new SpringCommandBus(List.of());

    assertThat(commandBus.getHandlerCount()).isEqualTo(0);
  }

  @Test
  void shouldThrowWhenDuplicateHandlersRegistered() {
    TestCommandHandler handler1 = new TestCommandHandler();
    TestCommandHandler handler2 = new TestCommandHandler();

    assertThatThrownBy(() -> new SpringCommandBus(List.of(handler1, handler2)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Multiple handlers registered for command TestCommand")
        .hasMessageContaining("TestCommandHandler");
  }

  @Test
  void shouldExecuteCommand() {
    TestCommandHandler handler = new TestCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler));

    CommandResult<String> result = commandBus.execute(new TestCommand("test"));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.orElseThrow()).isEqualTo("handled-test");
  }

  @Test
  void shouldThrowWhenNullCommandDispatched() {
    SpringCommandBus commandBus = new SpringCommandBus(List.of());

    assertThatThrownBy(() -> commandBus.execute(null)).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Command must not be null");
  }

  @Test
  void shouldExecuteMultipleCommands() {
    TestCommandHandler handler1 = new TestCommandHandler();
    AnotherCommandHandler handler2 = new AnotherCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler1, handler2));

    CommandResult<String> result1 = commandBus.execute(new TestCommand("first"));
    CommandResult<Integer> result2 = commandBus.execute(new AnotherCommand(5));

    assertThat(result1.orElseThrow()).isEqualTo("handled-first");
    assertThat(result2.orElseThrow()).isEqualTo(10);
  }

  @Test
  void shouldThrowWhenNoHandlerFound() {
    SpringCommandBus commandBus = new SpringCommandBus(List.of());

    assertThatThrownBy(() -> commandBus.execute(new TestCommand("test")))
        .isInstanceOf(CommandHandlerNotFoundException.class).hasMessageContaining("No handler found for command")
        .hasMessageContaining("TestCommand");
  }

  @Test
  void shouldThrowWhenHandlerThrowsException() {
    FailingCommandHandler handler = new FailingCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler));

    assertThatThrownBy(() -> commandBus.execute(new TestCommand("test"))).isInstanceOf(CommandExecutionException.class)
        .hasMessageContaining("Error executing command: TestCommand").hasCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("Handler failure");
  }

  // Custom command with overridden name
  static class CustomNameCommand implements Command<String> {
    private final String value;

    CustomNameCommand(String value) {
      this.value = value;
    }

    @Override
    public String getCommandName() {
      return "CustomCommandName";
    }

    public String getValue() {
      return value;
    }
  }

  static class CustomNameCommandHandler implements CommandHandler<CustomNameCommand, String> {
    @Override
    public CommandResult<String> handle(CustomNameCommand command) {
      return new CommandResult.Success<>("custom-" + command.getValue());
    }

    @Override
    public Class<CustomNameCommand> getCommandClass() {
      return CustomNameCommand.class;
    }
  }

  @Test
  void shouldUseCustomCommandName() {
    CustomNameCommandHandler handler = new CustomNameCommandHandler();
    SpringCommandBus commandBus = new SpringCommandBus(List.of(handler));

    // Should execute successfully with custom command name
    CommandResult<String> result = commandBus.execute(new CustomNameCommand("test"));
    assertThat(result.isSuccess()).isTrue();
    assertThat(result.orElseThrow()).isEqualTo("custom-test");
  }
}
