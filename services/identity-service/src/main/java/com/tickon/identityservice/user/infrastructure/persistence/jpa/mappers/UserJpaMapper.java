package com.tickon.identityservice.user.infrastructure.persistence.jpa.mappers;

import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.valueobjects.Email;
import com.tickon.identityservice.user.domain.valueobjects.PasswordHash;
import com.tickon.identityservice.user.domain.valueobjects.UserId;
import com.tickon.identityservice.user.domain.valueobjects.Username;
import com.tickon.identityservice.user.infrastructure.persistence.jpa.JpaUserEntity;

public class UserJpaMapper {
  public JpaUserEntity toEntity(User user) {
    var entity = new JpaUserEntity();
    entity.id = user.id().value().toString();
    entity.firstName = user.firstName();
    entity.lastName = user.lastName();
    entity.username = user.username().value();
    entity.email = user.email().value();
    entity.passwordHash = user.passwordHash().value();
    entity.createdAt = user.createdAt();
    entity.updatedAt = user.updatedAt();
    entity.isDeleted = user.isDeleted();
    entity.deletedAt = user.deletedAt();
    return entity;
  }

  public User toDomain(JpaUserEntity entity) {
    return User.fromPersistence(
        UserId.from(entity.id),
        new Email(entity.email),
        new Username(entity.username),
        entity.firstName,
        entity.lastName,
        new PasswordHash(entity.passwordHash),
        entity.createdAt,
        entity.updatedAt,
        entity.isDeleted,
        entity.deletedAt);
  }
}
