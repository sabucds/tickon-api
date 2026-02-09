package com.tickon.identity.auth.application.ports.out;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.AuthUser;
import java.util.Optional;

public interface AuthUserRepository {
  Optional<AuthUser> findById(UserId id);

  Optional<AuthUser> findByUsernameOrEmail(String usernameOrEmail);

}
