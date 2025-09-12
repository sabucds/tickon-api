package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<JpaUserEntity, String> {
  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
