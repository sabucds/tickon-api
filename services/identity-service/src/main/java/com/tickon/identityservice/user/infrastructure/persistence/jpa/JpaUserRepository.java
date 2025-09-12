package com.tickon.identityservice.user.infrastructure.persistence.jpa;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.Email;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.UserId;
import com.tickon.identityservice.user.domain.Username;

@Repository
public class JpaUserRepository implements UserRepository {

  private final SpringDataUserRepository springRepository;
  private final UserJpaMapper mapper;

  public JpaUserRepository(SpringDataUserRepository springRepository) {
    this.springRepository = springRepository;
    this.mapper = new UserJpaMapper();
  }

  @Override
  public void save(User user) {
    JpaUserEntity entity = mapper.toEntity(user);
    springRepository.save(entity);
  }

  @Override
  public Optional<User> findById(UserId id) {
    return springRepository.findById(id.toString()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(Email email) {
    return springRepository.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return springRepository.existsByUsername(username.value());
  }
}
