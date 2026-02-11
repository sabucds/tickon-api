package com.tickon.identity.user.infrastructure.persistence.mappers;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.identity.domain.valueobjects.UserStatus;
import com.tickon.identity.shared.infrastructure.persistence.entities.UserEntity;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Username;

public class UserPersistenceMapper {
  public UserEntity toEntity(User user) {
    var entity = new UserEntity();
    entity.id = user.id().value();
    entity.firstName = user.firstName();
    entity.lastName = user.lastName();
    entity.username = user.username().value();
    entity.email = user.email().value();
    entity.passwordHash = user.passwordHash().value();
    entity.status = user.status().name();
    return entity;
  }

  public User toDomain(UserEntity entity) {
    return User.restore(new UserId(entity.id), Email.from(entity.email), Username.from(entity.username),
        entity.firstName, entity.lastName, new PasswordHash(entity.passwordHash), UserStatus.valueOf(entity.status));
  }
}
