package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.infrastructure.persistence.mappers.PasswordResetTokenPersistenceMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PasswordResetTokenRepositoryAdapter implements ResetTokenRepository {

  private final JpaPasswordResetTokenRepository jpaRepository;
  private final PasswordResetTokenPersistenceMapper mapper;

  public PasswordResetTokenRepositoryAdapter(JpaPasswordResetTokenRepository jpaRepository,
      PasswordResetTokenPersistenceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void save(PasswordResetToken token) {
    jpaRepository.save(mapper.toEntity(token));
  }

  @Override
  public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void invalidateAllForUser(UUID userId, Instant now) {
    jpaRepository.invalidateAllForUser(userId, now);
  }
}
