package com.tickon.common.commands;

public interface Command<R> {
  default String getCommandName() {
    return this.getClass().getSimpleName();
  }
}
