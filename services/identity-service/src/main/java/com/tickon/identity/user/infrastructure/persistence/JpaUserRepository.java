package com.tickon.identity.user.infrastructure.persistence;

import com.tickon.identity.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
