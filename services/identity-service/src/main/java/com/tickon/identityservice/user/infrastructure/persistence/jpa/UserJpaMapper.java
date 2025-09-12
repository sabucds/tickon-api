package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import com.tickon.identityservice.user.domain.Email;
import com.tickon.identityservice.user.domain.PasswordHash;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.UserId;
import com.tickon.identityservice.user.domain.Username;

class UserJpaMapper {
  JpaUserEntity toEntity(User user) {
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

  User toDomain(JpaUserEntity entity) {
    return User.fromPersistence(UserId.from(entity.id), new Email(entity.email),
        new Username(entity.username), entity.firstName, entity.lastName,
        new PasswordHash(entity.passwordHash), entity.createdAt, entity.updatedAt, entity.isDeleted,
        entity.deletedAt);
  }
}
