package com.tickon.identity.user.infrastructure.persistence.mappers;

import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import com.tickon.identity.user.infrastructure.persistence.entities.UserEntity;

public class UserPersistenceMapper {
  public UserEntity toEntity(User user) {
    var entity = new UserEntity();
    entity.id = user.id().value();
    entity.firstName = user.firstName();
    entity.lastName = user.lastName();
    entity.username = user.username().value();
    entity.email = user.email().value();
    entity.passwordHash = user.passwordHash().value();
    return entity;
  }

  public User toDomain(UserEntity entity) {
    return User.fromPersistence(new UserId(entity.id), Email.from(entity.email), Username.from(entity.username),
        entity.firstName, entity.lastName, new PasswordHash(entity.passwordHash));
  }
}
