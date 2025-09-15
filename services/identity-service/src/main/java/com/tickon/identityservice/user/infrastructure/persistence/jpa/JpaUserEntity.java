package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
class JpaUserEntity {
  @Id String id;

  @Column(name = "username", unique = true)
  String username;

  @Column(name = "first_name")
  String firstName;

  @Column(name = "last_name")
  String lastName;

  @Column(name = "email", unique = true, nullable = false)
  String email;

  @Column(name = "password_hash")
  String passwordHash;

  @Column(name = "created_at")
  Instant createdAt;

  @Column(name = "updated_at")
  Instant updatedAt;

  @Column(name = "is_deleted")
  boolean isDeleted;

  @Column(name = "deleted_at")
  Instant deletedAt;

  protected JpaUserEntity() {}
}
