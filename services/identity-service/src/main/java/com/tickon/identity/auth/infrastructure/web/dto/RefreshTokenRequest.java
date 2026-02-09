package com.tickon.identity.auth.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {}
