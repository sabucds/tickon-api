package com.tickon.identity.user.application.services;

import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.in.RegisterUserUseCase;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;

  public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  public UserResult register(RegisterUserCommand request) {
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

    User user = User.forRegistration(UserId.generate(), email, username, request.firstName(), request.lastName(), hash);

    userRepository.save(user);
    return UserResult.from(user);
  }
}
