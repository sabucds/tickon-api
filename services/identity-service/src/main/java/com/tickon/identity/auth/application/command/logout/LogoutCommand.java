package com.tickon.identity.auth.application.command.logout;

import com.tickon.common.commands.Command;

public record LogoutCommand(String refreshToken) implements Command<Void> {}
