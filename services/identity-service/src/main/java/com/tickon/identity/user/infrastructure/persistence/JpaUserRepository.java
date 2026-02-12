package com.tickon.identity.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tickon.identity.user.infrastructure.persistence.entities.UserEntity;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<UserEntity> findByUsernameOrEmail(String username, String email);

  Optional<UserEntity> findByEmail(String email);
}
