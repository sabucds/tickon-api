package com.tickon.identity.auth.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sessions")
public class SessionEntity {
  @Id
  public String id;

  @Column(name = "refresh_token_hash", nullable = false, unique = true)
  public String refreshTokenHash;

  @Column(name = "user_id", nullable = false)
  public String userId;

  @Column(name = "family_id", nullable = false)
  public String familyId;

  @Column(name = "device_id", nullable = false)
  public String deviceId;

  @Column(name = "rotated_from_session_id")
  public String rotatedFromSessionId;

  @Column(name = "expires_at", nullable = false)
  public Instant expiresAt;

  @Column(name = "revoked_at")
  public Instant revokedAt;

  @Column(name = "revoke_reason")
  public String revokeReason;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

}
