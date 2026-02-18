package com.tickon.common.commands.exceptions;

public abstract class CommandBusException extends RuntimeException {
  protected CommandBusException(String message) {
    super(message);
  }

  protected CommandBusException(String message, Throwable cause) {
    super(message, cause);
  }
}
