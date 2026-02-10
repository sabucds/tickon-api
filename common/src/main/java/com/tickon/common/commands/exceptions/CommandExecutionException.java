package com.tickon.common.commands.exceptions;

public class CommandExecutionException extends CommandBusException {
  public CommandExecutionException(Class<?> commandClass, Throwable cause) {
    super(String.format("Error executing command: %s", commandClass.getSimpleName()), cause);
  }
}
