package com.tickon.identityservice.user.application.models;

import com.tickon.identityservice.user.domain.Email;
import com.tickon.identityservice.user.domain.Username;

public record RegisterUserRequest(
    String firstName, String lastName, Username username, Email email, String rawPassword) {}
