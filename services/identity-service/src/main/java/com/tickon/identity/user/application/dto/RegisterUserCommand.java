package com.tickon.identity.user.application.dto;

import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.Username;

public record RegisterUserCommand(String firstName, String lastName, Username username, Email email,
    String rawPassword) {}