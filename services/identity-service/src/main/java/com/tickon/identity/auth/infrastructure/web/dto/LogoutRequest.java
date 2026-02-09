package com.tickon.identity.auth.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {}
