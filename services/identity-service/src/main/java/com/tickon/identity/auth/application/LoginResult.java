package com.tickon.identity.auth.application;

public record LoginResult(String accessToken, String refreshToken) {}
