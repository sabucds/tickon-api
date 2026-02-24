package com.tickon.identity.auth.application.command.login;

import com.tickon.common.commands.Command;
import com.tickon.identity.auth.application.LoginResult;

public record LoginCommand(String usernameOrEmail, String password, String deviceId) implements Command<LoginResult> {}
