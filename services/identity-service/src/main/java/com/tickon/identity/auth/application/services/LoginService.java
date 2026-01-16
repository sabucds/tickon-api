package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
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
  private final Clock clock;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Duration sessionDuration;

  public LoginService(UserRepository userRepository, SessionRepository sessionRepository, PasswordHasher passwordHasher,
      TokenProvider tokenProvider, @Value("${security.jwt.session-expiration-ms}") long sessionExpirationMs,
      java.time.Clock clock, RefreshTokenHasher refreshTokenHasher) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.sessionDuration = Duration.ofMillis(sessionExpirationMs);
  }

  @Override
  public LoginResult login(LoginCommand cmd) {
    User user = userRepository.findByUsernameOrEmail(cmd.usernameOrEmail())
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordHasher.verify(cmd.password(), user.passwordHash())) {
      throw new InvalidCredentialsException();
    }

    String accessToken = tokenProvider.generateAccessToken(user);
    String refreshToken = tokenProvider.generateRefreshToken(user);
    RefreshTokenHash refreshTokenHash = refreshTokenHasher.hash(refreshToken);

    Instant now = clock.instant();
    Session session = Session.create(SessionId.generate(), refreshTokenHash, user.id(), cmd.deviceId(),
        FamilyId.generate(), null, sessionDuration, now);

    sessionRepository.save(session);

    return new LoginResult(accessToken, refreshToken);
  }
}
