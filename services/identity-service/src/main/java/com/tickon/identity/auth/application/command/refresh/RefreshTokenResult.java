package com.tickon.identity.auth.application.command.refresh;

public record RefreshTokenResult(String accessToken, String refreshToken) {}
