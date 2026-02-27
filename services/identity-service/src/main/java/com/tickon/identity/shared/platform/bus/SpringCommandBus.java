package com.tickon.identity.shared.platform.bus;

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
    if (command == null)
      throw new IllegalArgumentException("Command must not be null");
    CommandHandler<Command<R>, R> handler = (CommandHandler<Command<R>, R>) handlers.get(command.getClass());
    if (handler == null) {
      throw new CommandHandlerNotFoundException(command.getClass());
    }
    try {
      return handler.handle(command);
    } catch (CommandHandlerNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new CommandExecutionException(command.getClass(), e);
    }
  }

  public int getHandlerCount() {
    return handlers.size();
  }
}
