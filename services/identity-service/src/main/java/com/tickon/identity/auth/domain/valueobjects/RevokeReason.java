package com.tickon.identity.auth.domain.valueobjects;

public enum RevokeReason {
  SESSION_ROTATED, USER_LOGOUT, TOKEN_COMPROMISED, ADMIN_ACTION, OTHER
}