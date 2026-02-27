package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.application.ports.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import com.tickon.identity.auth.infrastructure.persistence.mappers.SessionPersistenceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SessionRepositoryAdapter implements SessionRepository {

  private final JpaSessionRepository jpaRepository;
  private final SessionPersistenceMapper mapper;

  public SessionRepositoryAdapter(JpaSessionRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new SessionPersistenceMapper();
  }

  @Override
  public void save(Session session) {
    SessionEntity entity = mapper.toEntity(session);
    jpaRepository.save(entity);
  }

  @Override
  public Optional<Session> findById(SessionId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Session> findByRefreshTokenHash(String refreshToken) {
    return jpaRepository.findByRefreshTokenHash(refreshToken).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void revokeAllByFamilyId(FamilyId familyId, Instant revokedAt, RevokeReason reason) {
    jpaRepository.revokeAllByFamilyId(familyId.value(), revokedAt, reason.name());
  }

  @Override
  @Transactional
  public void revokeAllByUserId(UUID userId, Instant now, RevokeReason reason) {
    List<SessionEntity> sessions = jpaRepository.findByUserIdAndRevokedAtIsNull(userId);
    for (SessionEntity entity : sessions) {
      Session session = mapper.toDomain(entity);
      if (!session.isRevoked()) {
        session.revoke(now, reason);
        jpaRepository.save(mapper.toEntity(session));
      }
    }
  }

}
