package com.tickon.identity.user.application.dto;

public record RegisterUserCommand(String firstName, String lastName, String username, String email,
    String rawPassword) {}