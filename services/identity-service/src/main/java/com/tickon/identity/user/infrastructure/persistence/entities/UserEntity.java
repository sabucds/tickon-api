package com.tickon.identity.user.infrastructure.persistence.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class UserEntity {
  @Id
  public UUID id;

  @Column(name = "username", nullable = false, columnDefinition = "citext")
  public String username;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  public String email;

  @Column(name = "email_verified", nullable = false)
  public boolean emailVerified = false;

  @Column(name = "status", nullable = false)
  public String status;

  @Basic(fetch = FetchType.LAZY)
  @Column(name = "password_hash", nullable = false)
  public String passwordHash;

  @Column(name = "first_name")
  public String firstName;

  @Column(name = "last_name")
  public String lastName;

  @Column(name = "created_at", nullable = false, updatable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  @Column(name = "is_deleted", nullable = false)
  public boolean isDeleted = false;

  @Column(name = "deleted_at")
  public Instant deletedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null)
      createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}
