package com.tickon.identity.auth.domain.valueobjects;

public enum RevokeReason {
  SESSION_ROTATED, USER_LOGOUT, TOKEN_COMPROMISED, TOKEN_REUSE_DETECTED, ADMIN_ACTION, PASSWORD_RESET, OTHER
}