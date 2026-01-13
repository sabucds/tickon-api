package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import com.tickon.identity.user.domain.valueobjects.UserId;

public class SessionPersistenceMapper {
  public SessionEntity toEntity(Session session) {
    var entity = new SessionEntity();
    entity.id = session.id().value();
    entity.refreshTokenHash = session.refreshTokenHash().value();
    entity.userId = session.userId().value();
    entity.familyId = session.familyId().value();
    entity.deviceId = session.deviceId();
    entity.rotatedFromSessionId = session.rotatedFromSessionId() != null ? session.rotatedFromSessionId().value()
        : null;
    entity.expiresAt = session.expiresAt();
    entity.revokedAt = session.revokedAt();
    entity.revokeReason = session.revokeReason() != null ? session.revokeReason().name() : null;
    return entity;
  }

  public Session toDomain(SessionEntity entity) {
    return Session.fromPersistence(new SessionId(entity.id), RefreshTokenHash.from(entity.refreshTokenHash),
        new UserId(entity.userId), entity.deviceId, new FamilyId(entity.familyId),
        entity.rotatedFromSessionId != null ? new SessionId(entity.rotatedFromSessionId) : null, entity.expiresAt,
        entity.revokedAt, entity.revokeReason != null ? RevokeReason.valueOf(entity.revokeReason) : null);
  }
}
