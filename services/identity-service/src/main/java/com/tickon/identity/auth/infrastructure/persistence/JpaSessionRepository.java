package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.infrastructure.persistence.entities.SessionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaSessionRepository extends JpaRepository<SessionEntity, UUID> {
  Optional<SessionEntity> findByRefreshTokenHash(String refreshToken);

  List<SessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

  @Modifying
  @Query("UPDATE SessionEntity s SET s.revokedAt = :revokedAt, s.revokeReason = :revokeReason "
      + "WHERE s.familyId = :familyId AND s.revokedAt IS NULL")
  void revokeAllByFamilyId(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt,
      @Param("revokeReason") String revokeReason);
}
