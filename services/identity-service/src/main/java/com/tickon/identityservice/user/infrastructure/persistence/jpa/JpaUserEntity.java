package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class JpaUserEntity {
  @Id public String id;

  @Column(name = "username", unique = true)
  public String username;

  @Column(name = "first_name")
  public String firstName;

  @Column(name = "last_name")
  public String lastName;

  @Column(name = "email", unique = true, nullable = false)
  public String email;

  @Column(name = "password_hash")
  public String passwordHash;

  @Column(name = "created_at")
  public Instant createdAt;

  @Column(name = "updated_at")
  public Instant updatedAt;

  @Column(name = "is_deleted")
  public boolean isDeleted;

  @Column(name = "deleted_at")
  public Instant deletedAt;

  public JpaUserEntity() {}
}
