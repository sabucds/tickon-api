package com.tickon.identity.user.application.services;

import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.RegisterUserUseCase;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;
  private final Clock clock;

  public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy, Clock clock) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
    this.clock = clock;
  }

  @Override
  public UserResult register(RegisterUserCommand request) {
    Email email = Email.from(request.email());
    Username username = Username.from(request.username());

    if (userRepository.existsByEmail(email)) {
      throw new DuplicateEmailException(email.value());
    }
    if (userRepository.existsByUsername(username)) {
      throw new DuplicateUsernameException(username.value());
    }

    passwordPolicy.validate(request.rawPassword());
    PasswordHash hash = passwordHasher.hash(request.rawPassword());

    Instant now = clock.instant();

    User user = User.create(UserId.generate(), email, username, request.firstName(), request.lastName(), hash, now);

    userRepository.save(user);
    return UserResult.from(user);
  }
}
