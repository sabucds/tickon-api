package com.tickon.common.commands.exceptions;

public class CommandHandlerNotFoundException extends CommandBusException {
  public CommandHandlerNotFoundException(Class<?> commandClass) {
    super(String.format("No handler found for command: %s", commandClass.getName()));
  }
}
