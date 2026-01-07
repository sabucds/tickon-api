package com.tickon.identity.auth.application.dto;

public record LoginCommand(String usernameOrEmail, String password, String deviceId) {}
