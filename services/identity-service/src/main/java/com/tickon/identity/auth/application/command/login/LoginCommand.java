package com.tickon.identity.auth.application.command.login;

import com.tickon.common.commands.Command;

public record LoginCommand(String usernameOrEmail, String password, String deviceId) implements Command<LoginResult> {}
