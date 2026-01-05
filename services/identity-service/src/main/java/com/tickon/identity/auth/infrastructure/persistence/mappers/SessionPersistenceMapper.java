package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import com.tickon.identity.user.domain.valueobjects.UserId;

public class SessionPersistenceMapper {
  public SessionEntity toEntity(Session session) {
    var entity = new SessionEntity();
    entity.id = session.id().value().toString();
    entity.refreshToken = session.refreshToken();
    entity.userId = session.userId().toString();
    entity.expiresAt = session.expiresAt();
    entity.invalidatedAt = session.invalidatedAt();
    entity.createdAt = session.createdAt();
    entity.updatedAt = session.updatedAt();
    entity.isValid = session.isValid();
    return entity;
  }

  public Session toDomain(SessionEntity entity) {
    return Session.fromPersistence(SessionId.from(entity.id), entity.refreshToken, UserId.from(entity.userId),
        entity.expiresAt, entity.isValid, entity.createdAt, entity.updatedAt, entity.invalidatedAt);
  }
}
