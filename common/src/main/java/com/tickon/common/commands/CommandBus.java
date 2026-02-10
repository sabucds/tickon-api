package com.tickon.common.commands;

public interface CommandBus {
  <R> CommandResult<R> execute(Command<R> command);
}
