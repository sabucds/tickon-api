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

  @Column(name = "refresh_token", nullable = false)
  public String refreshToken;

  @Column(name = "user_id", nullable = false)
  public String userId;

  @Column(name = "expires_at", nullable = false)
  public Instant expiresAt;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  @Column(name = "is_valid", nullable = false)
  public boolean isValid = false;

  @Column(name = "invalidated_at")
  public Instant invalidatedAt;

  @Column(name = "deleted_at")
  public Instant deletedAt;

}
