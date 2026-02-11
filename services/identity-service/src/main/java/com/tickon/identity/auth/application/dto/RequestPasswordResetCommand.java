package com.tickon.identity.auth.application.dto;

import com.tickon.common.identity.domain.valueobjects.Email;

public record RequestPasswordResetCommand(Email email) {}
