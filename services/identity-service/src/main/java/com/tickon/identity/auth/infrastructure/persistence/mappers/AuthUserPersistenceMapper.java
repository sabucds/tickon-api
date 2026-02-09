package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.shared.infrastructure.persistence.entities.UserEntity;

public class AuthUserPersistenceMapper {

  public AuthUser toDomain(UserEntity entity) {
    return new AuthUser(new UserId(entity.id), new PasswordHash(entity.passwordHash),
        UserStatus.valueOf(entity.status));
  }
}
