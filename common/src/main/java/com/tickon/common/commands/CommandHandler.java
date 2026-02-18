package com.tickon.common.commands;

public interface CommandHandler<C extends Command<R>, R> {
  CommandResult<R> handle(C command);

  Class<C> getCommandClass();
}
