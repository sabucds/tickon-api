package com.tickon.identity.shared.infrastructure;

import com.tickon.common.commands.Command;
import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.commands.exceptions.CommandExecutionException;
import com.tickon.common.commands.exceptions.CommandHandlerNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SpringCommandBus implements CommandBus {
  private static final Logger log = LoggerFactory.getLogger(SpringCommandBus.class);

  private final Map<Class<?>, CommandHandler<?, ?>> handlers = new HashMap<>();

  public SpringCommandBus(List<CommandHandler<?, ?>> handlers) {
    log.info("Initializing CommandBus with {} handler(s)", handlers.size());

    Map<Class<?>, List<CommandHandler<?, ?>>> grouped = handlers.stream()
        .collect(Collectors.groupingBy(CommandHandler::getCommandClass));

    grouped.forEach((commandClass, handlerList) -> {
      if (handlerList.size() > 1) {
        String handlerNames = handlerList.stream().map(h -> h.getClass().getSimpleName())
            .collect(Collectors.joining(", "));
        String errorMsg = String.format(
            "Multiple handlers registered for command %s: [%s]. Only one handler per command is allowed.",
            commandClass.getSimpleName(), handlerNames);
        log.error(errorMsg);
        throw new IllegalStateException(errorMsg);
      }

      CommandHandler<?, ?> handler = handlerList.get(0);
      this.handlers.put(commandClass, handler);
      log.debug("Registered command handler: {} for command: {}", handler.getClass().getSimpleName(),
          commandClass.getSimpleName());
    });

    log.info("CommandBus initialized successfully with {} unique handler(s)", this.handlers.size());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> CommandResult<R> execute(Command<R> command) {
    String commandName = command.getCommandName();
    log.debug("Executing command: {}", commandName);
    long startTime = System.currentTimeMillis();

    try {
      CommandHandler<Command<R>, R> handler = (CommandHandler<Command<R>, R>) handlers.get(command.getClass());

      if (handler == null) {
        log.error("No handler found for command: {}", command.getClass().getName());
        throw new CommandHandlerNotFoundException(command.getClass());
      }

      CommandResult<R> result = handler.handle(command);
      long duration = System.currentTimeMillis() - startTime;

      logCommandResult(commandName, result, duration);

      return result;
    } catch (CommandHandlerNotFoundException e) {
      throw e;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Command execution failed: {} ({}ms)", commandName, duration, e);
      throw new CommandExecutionException(command.getClass(), e);
    }
  }

  private void logCommandResult(String commandName, CommandResult<?> result, long duration) {
    switch (result) {
    case CommandResult.Success<?> success ->
      log.debug("Command executed successfully: {} ({}ms)", commandName, duration);
    case CommandResult.Error<?> error ->
      log.warn("Command executed with error: {} ({}ms) - {}", commandName, duration, error.message());
    }
  }

  public int getHandlerCount() {
    return handlers.size();
  }
}
