package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import com.tickon.identity.user.domain.valueobjects.UserId;

public class SessionPersistenceMapper {
  public SessionEntity toEntity(Session session) {
    var entity = new SessionEntity();
    entity.id = session.id().value().toString();
    entity.refreshTokenHash = session.refreshTokenHash().value();
    entity.userId = session.userId().value().toString();
    entity.expiresAt = session.expiresAt();
    entity.revokedAt = session.revokedAt();
    entity.revokeReason = session.revokeReason() != null ? session.revokeReason().name() : null;
    entity.createdAt = session.createdAt();
    entity.updatedAt = session.updatedAt();
    return entity;
  }

  public Session toDomain(SessionEntity entity) {
    return Session.fromPersistence(SessionId.from(entity.id), RefreshTokenHash.from(entity.refreshTokenHash),
        UserId.from(entity.userId), entity.deviceId, FamilyId.from(entity.familyId),
        entity.rotatedFromSessionId != null ? SessionId.from(entity.rotatedFromSessionId) : null, entity.expiresAt,
        entity.createdAt, entity.updatedAt, entity.revokedAt);
  }
}
