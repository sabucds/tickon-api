package com.tickon.identity.user.infrastructure.persistence;

import com.tickon.identity.user.infrastructure.persistence.entities.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  Optional<UserEntity> findByUsernameOrEmail(String username, String email);

  Optional<UserEntity> findByEmail(String email);
}
