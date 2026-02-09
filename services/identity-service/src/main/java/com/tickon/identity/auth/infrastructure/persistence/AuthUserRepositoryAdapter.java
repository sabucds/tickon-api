package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.ports.out.AuthUserRepository;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.infrastructure.persistence.mappers.AuthUserPersistenceMapper;
import com.tickon.identity.shared.infrastructure.persistence.repositories.JpaUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuthUserRepositoryAdapter implements AuthUserRepository {

  private final JpaUserRepository jpaRepository;
  private final AuthUserPersistenceMapper mapper;

  public AuthUserRepositoryAdapter(JpaUserRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new AuthUserPersistenceMapper();
  }

  @Override
  public Optional<AuthUser> findById(UserId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<AuthUser> findByUsernameOrEmail(String usernameOrEmail) {
    return jpaRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).map(mapper::toDomain);
  }
}
