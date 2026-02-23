package com.tickon.identity.auth.domain;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import java.util.Objects;

public class AuthUser {
  private final UserId id;
  private PasswordHash passwordHash;
  private UserStatus status;

  public AuthUser(UserId id, PasswordHash passwordHash, UserStatus status) {
    this.id = Objects.requireNonNull(id, "id");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.status = Objects.requireNonNull(status, "status");
  }

  public static AuthUser fromDTO(UserAuthDataDTO dto) {
    return new AuthUser(new UserId(dto.id()), new PasswordHash(dto.passwordHash()), UserStatus.valueOf(dto.status()));
  }

  public UserId id() {
    return id;
  }

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public UserStatus status() {
    return status;
  }
}
