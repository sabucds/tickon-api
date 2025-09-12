// user/application/use-cases/RegisterUser.java
package com.tickon.identityservice.user.application.services;

import java.time.Clock;
import com.tickon.identityservice.user.application.models.RegisterUserRequest;
import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserUseCase;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.PasswordHash;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.UserId;
import com.tickon.identityservice.user.domain.policies.PasswordStrengthPolicy;

public class RegisterUser implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;

  public RegisterUser(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy, Clock clock) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  public UserResponse register(RegisterUserRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalStateException("Email already in use");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalStateException("Username already in use");
    }

    passwordPolicy.validate(request.rawPassword());
    PasswordHash hash = passwordHasher.hash(request.rawPassword());
    User user = User.forRegistration(UserId.generate(), request.email(), request.username(),
        request.firstName(), request.lastName(), hash);

    userRepository.save(user);
    return UserResponse.from(user);
  }
}
