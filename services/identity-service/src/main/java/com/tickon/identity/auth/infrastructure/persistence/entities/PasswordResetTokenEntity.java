package com.tickon.identity.auth.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

  @Id
  public UUID id;

  @Column(name = "token_hash", nullable = false, unique = true)
  public String tokenHash;

  @Column(name = "user_id", nullable = false)
  public UUID userId;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  public String email;

  @Column(name = "absolute_expires_at", nullable = false)
  public Instant absoluteExpiresAt;

  @Column(name = "used_at")
  public Instant usedAt;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    updatedAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}
