package com.tickon.identity.auth.application.command.login;

public record LoginResult(String accessToken, String refreshToken) {}
