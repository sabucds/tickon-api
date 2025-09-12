// user/application/port/out/UserRepository.java
package com.tickon.identityservice.user.application.ports.outbound;

import java.util.Optional;
import com.tickon.identityservice.user.domain.Email;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.UserId;
import com.tickon.identityservice.user.domain.Username;

public interface UserRepository {
  Optional<User> findById(UserId id);

  boolean existsByEmail(Email email);

  boolean existsByUsername(Username username);

  void save(User user);
}
