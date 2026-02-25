package com.tickon.identity.auth.application.command.refresh;

import com.tickon.common.commands.Command;

public record RefreshTokenCommand(String refreshToken) implements Command<RefreshTokenResult> {}
