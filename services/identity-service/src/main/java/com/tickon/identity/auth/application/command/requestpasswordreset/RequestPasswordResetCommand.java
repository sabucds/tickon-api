package com.tickon.identity.auth.application.command.requestpasswordreset;

import com.tickon.common.commands.Command;

public record RequestPasswordResetCommand(String email) implements Command<RequestPasswordResetResult> {}
