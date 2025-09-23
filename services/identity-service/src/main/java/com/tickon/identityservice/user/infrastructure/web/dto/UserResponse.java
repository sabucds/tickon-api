package com.tickon.identityservice.user.infrastructure.web.dto;

public record UserResponse(String id, String firstName, String lastName, String username, String email) {}
