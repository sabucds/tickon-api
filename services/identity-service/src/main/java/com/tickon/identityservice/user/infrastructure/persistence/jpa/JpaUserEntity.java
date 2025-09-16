package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class JpaUserEntity {
  @Id public String id;

  @Column(name = "username", nullable = false, columnDefinition = "citext")
  public String username;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  public String email;

  @Column(name = "password_hash")
  public String passwordHash;

  @Column(name = "first_name")
  public String firstName;

  @Column(name = "last_name")
  public String lastName;

  @Column(name = "created_at")
  public Instant createdAt;

  @Column(name = "updated_at")
  public Instant updatedAt;

  @Column(name = "is_deleted", nullable = false)
  public boolean isDeleted = false;

  @Column(name = "deleted_at")
  public Instant deletedAt;

  protected JpaUserEntity() {}
}
