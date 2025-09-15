package com.tickon.identityservice.user.infrastructure.web.dto;

public record RegisterUserRequest(
    String firstName, String lastName, String username, String email, String password) {}
