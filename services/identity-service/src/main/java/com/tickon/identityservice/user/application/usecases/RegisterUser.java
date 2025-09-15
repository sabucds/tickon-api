package com.tickon.identityservice.user.application.usecases;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identityservice.user.domain.valueobjects.Email;
import com.tickon.identityservice.user.domain.valueobjects.PasswordHash;
import com.tickon.identityservice.user.domain.valueobjects.UserId;
import com.tickon.identityservice.user.domain.valueobjects.Username;

import java.time.Clock;
import java.time.Instant;

public class RegisterUser implements RegisterUserService {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;
  private final Clock clock;

  public RegisterUser(
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
    this.clock = clock;
  }

  @Override
  public UserResponseModel register(RegisterUserCommand request) {
    Email email = Email.from(request.email());
    Username username = Username.from(request.username());

    if (userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already in use");
    }
    if (userRepository.existsByUsername(username)) {
      throw new IllegalStateException("Username already in use");
    }

    passwordPolicy.validate(request.rawPassword());
    PasswordHash hash = passwordHasher.hash(request.rawPassword());

    Instant now = Instant.now(clock);
    User user =
        User.forRegistration(
            UserId.generate(), email, username, request.firstName(), request.lastName(), hash, now);

    userRepository.save(user);
    return UserResponseModel.from(user);
  }
}
