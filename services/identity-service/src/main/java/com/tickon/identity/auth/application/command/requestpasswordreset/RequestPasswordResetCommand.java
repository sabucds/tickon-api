package com.tickon.identity.auth.application.command.requestpasswordreset;

import com.tickon.common.commands.Command;
import com.tickon.common.identity.domain.valueobjects.Email;

public record RequestPasswordResetCommand(Email email) implements Command<RequestPasswordResetResult> {}
