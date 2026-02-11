package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.infrastructure.persistence.entities.PasswordResetTokenEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE PasswordResetTokenEntity t SET t.usedAt = :now WHERE t.userId = :userId AND t.usedAt IS NULL")
  void invalidateAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
