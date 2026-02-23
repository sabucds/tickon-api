package com.tickon.identity.user.application.command.delete;

import com.tickon.common.commands.Command;
import com.tickon.common.identity.domain.valueobjects.UserId;

public record DeleteUserCommand(UserId userId) implements Command<Void> {}
