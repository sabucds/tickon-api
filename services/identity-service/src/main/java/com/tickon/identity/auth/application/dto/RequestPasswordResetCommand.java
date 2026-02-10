package com.tickon.identity.auth.application.dto;

import com.tickon.identity.user.domain.valueobjects.Email;

public record RequestPasswordResetCommand(Email email) {}
