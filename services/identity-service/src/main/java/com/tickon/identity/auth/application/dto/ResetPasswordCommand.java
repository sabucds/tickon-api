package com.tickon.identity.auth.application.dto;

public record ResetPasswordCommand(String resetToken, String newPassword) {}
