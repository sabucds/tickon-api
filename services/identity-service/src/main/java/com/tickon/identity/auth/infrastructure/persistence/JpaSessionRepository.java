package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSessionRepository extends JpaRepository<SessionEntity, String> {
  Optional<SessionEntity> findByRefreshTokenHash(String refreshToken);
}
