package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final Duration sessionTtl;
  private final Clock clock;

  public LoginService(UserRepository userRepository, SessionRepository sessionRepository, PasswordHasher passwordHasher,
      TokenProvider tokenProvider, @Value("${security.jwt.session-expiration}") Duration sessionTtl,
      java.time.Clock clock) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.sessionTtl = sessionTtl;
    this.clock = clock;
  }

  @Override
  public LoginResult login(LoginCommand command) {
    User user = userRepository.findByUsernameOrEmail(command.usernameOrEmail())
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordHasher.verify(command.password(), user.passwordHash())) {
      throw new InvalidCredentialsException();
    }

    String accessToken = tokenProvider.generateAccessToken(user);
    String refreshToken = tokenProvider.generateRefreshToken(user);

    Instant now = clock.instant();
    Session session = Session.create(SessionId.generate(), refreshToken, user.id(), now, sessionTtl);

    sessionRepository.save(session);

    return new LoginResult(accessToken, refreshToken);
  }
}
