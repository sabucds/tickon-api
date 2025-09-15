package com.tickon.identityservice.user.application.ports.outbound;

import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.valueobjects.Email;
import com.tickon.identityservice.user.domain.valueobjects.UserId;
import com.tickon.identityservice.user.domain.valueobjects.Username;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findById(UserId id);

  boolean existsByEmail(Email email);

  boolean existsByUsername(Username username);

  void save(User user);
}
