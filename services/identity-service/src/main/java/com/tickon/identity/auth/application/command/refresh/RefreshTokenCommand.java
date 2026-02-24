package com.tickon.identity.auth.application.command.refresh;

import com.tickon.common.commands.Command;
import com.tickon.identity.auth.application.LoginResult;

public record RefreshTokenCommand(String refreshToken) implements Command<LoginResult> {}
