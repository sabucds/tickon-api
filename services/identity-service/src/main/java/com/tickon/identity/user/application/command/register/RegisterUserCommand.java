package com.tickon.identity.user.application.command.register;

import com.tickon.common.commands.Command;
import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.domain.valueobjects.Username;

public record RegisterUserCommand(String firstName, String lastName, Username username, Email email, String rawPassword)
    implements Command<UserResult> {}
