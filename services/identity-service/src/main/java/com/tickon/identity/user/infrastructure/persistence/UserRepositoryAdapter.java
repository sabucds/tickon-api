package com.tickon.identity.user.infrastructure.persistence;

import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import com.tickon.identity.user.infrastructure.persistence.entities.UserEntity;
import com.tickon.identity.user.infrastructure.persistence.mappers.UserPersistenceMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

  private final JpaUserRepository springRepository;
  private final UserPersistenceMapper mapper;

  public UserRepositoryAdapter(JpaUserRepository springRepository) {
    this.springRepository = springRepository;
    this.mapper = new UserPersistenceMapper();
  }

  @Override
  public void save(User user) {
    UserEntity entity = mapper.toEntity(user);
    springRepository.save(entity);
  }

  @Override
  public Optional<User> findById(UserId id) {
    return springRepository.findById(id.value().toString()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return springRepository.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return springRepository.existsByUsername(username.value());
  }

  @Override
  public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
    return springRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).map(mapper::toDomain);
  }
}
