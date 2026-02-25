package com.tickon.identity.user.application.ports;

import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findById(UserId id);

  boolean existsByEmail(Email email);

  boolean existsByUsername(Username username);

  Optional<User> findByUsernameOrEmail(String usernameOrEmail);

  Optional<User> findByEmail(Email email);

  void save(User user);

  void delete(UserId id);
}
