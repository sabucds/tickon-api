package com.tickon.identity.auth.infrastructure.web.dto;

public record LoginResponse(String accessToken, String refreshToken) {}