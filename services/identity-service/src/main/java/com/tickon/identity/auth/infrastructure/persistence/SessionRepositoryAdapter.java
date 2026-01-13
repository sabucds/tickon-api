package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import com.tickon.identity.auth.infrastructure.persistence.mappers.SessionPersistenceMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepositoryAdapter implements SessionRepository {

  private final JpaSessionRepository springRepository;
  private final SessionPersistenceMapper mapper;

  public SessionRepositoryAdapter(JpaSessionRepository springRepository) {
    this.springRepository = springRepository;
    this.mapper = new SessionPersistenceMapper();
  }

  @Override
  public void save(Session session) {
    SessionEntity entity = mapper.toEntity(session);
    springRepository.save(entity);
  }

  @Override
  public Optional<Session> findById(SessionId id) {
    return springRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Session> findByRefreshTokenHash(String refreshToken) {
    return springRepository.findByRefreshTokenHash(refreshToken).map(mapper::toDomain);
  }

}
