package com.tickon.identity.user.infrastructure.persistence;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.shared.infrastructure.persistence.entities.UserEntity;
import com.tickon.identity.shared.infrastructure.persistence.repositories.JpaUserRepository;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.Username;
import com.tickon.identity.user.infrastructure.persistence.mappers.UserPersistenceMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository jpaRepository;
  private final UserPersistenceMapper mapper;

  public UserRepositoryAdapter(JpaUserRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new UserPersistenceMapper();
  }

  @Override
  public void save(User user) {
    UserEntity entity = mapper.toEntity(user);
    jpaRepository.save(entity);
  }

  @Override
  public Optional<User> findById(UserId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return jpaRepository.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return jpaRepository.existsByUsername(username.value());
  }

  @Override
  public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
    return jpaRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public void delete(UserId id) {
    jpaRepository.deleteById(id.value());
  }

}
