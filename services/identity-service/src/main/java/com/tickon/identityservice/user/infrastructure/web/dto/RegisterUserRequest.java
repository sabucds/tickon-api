package com.tickon.identityservice.user.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank(message = "First name is required") @Size(max = 50, message = "First name must not exceed 50 characters") String firstName,

    @NotBlank(message = "Last name is required") @Size(max = 50, message = "Last name must not exceed 50 characters") String lastName,

    @NotBlank(message = "Username is required") @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") String username,

    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

    @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password) {}
