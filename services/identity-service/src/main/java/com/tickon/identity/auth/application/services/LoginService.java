package com.tickon.identity.auth.application.services;

import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.application.dto.*;
import com.tickon.identity.auth.application.ports.in.LoginUseCase;
import com.tickon.identity.auth.application.ports.out.*;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.*;
import com.tickon.identity.shared.contracts.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.out.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

  private static final Logger log = LoggerFactory.getLogger(LoginService.class);

  private final QueryBus queryBus;
  private final SessionRepository sessionRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final Clock clock;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Duration sessionDuration;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public LoginService(QueryBus queryBus, SessionRepository sessionRepository, PasswordHasher passwordHasher,
      TokenProvider tokenProvider, @Value("${security.jwt.session-expiration-ms}") long sessionExpirationMs,
      java.time.Clock clock, RefreshTokenHasher refreshTokenHasher, DomainEventPublisher eventPublisher,
      IdentityMetrics metrics) {
    this.queryBus = queryBus;
    this.sessionRepository = sessionRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.sessionDuration = Duration.ofMillis(sessionExpirationMs);
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  public LoginResult login(LoginCommand cmd) {
    log.info("Login attempt for user '{}'", cmd.usernameOrEmail());

    Optional<UserAuthDataDTO> userOpt = queryBus.execute(new GetUserByUsernameOrEmailQuery(cmd.usernameOrEmail()))
        .orElseThrow();

    if (userOpt.isEmpty()) {
      log.warn("Login failed: user not found for '{}'", cmd.usernameOrEmail());
      metrics.loginAttempt("failure").increment();
      metrics.loginFailure("user_not_found").increment();
      throw new InvalidCredentialsException();
    }

    AuthUser user = AuthUser.fromDTO(userOpt.get());

    if (!passwordHasher.verify(cmd.password(), user.passwordHash())) {
      log.warn("Login failed: invalid credentials for '{}'", cmd.usernameOrEmail());
      metrics.loginAttempt("failure").increment();
      metrics.loginFailure("invalid_credentials").increment();
      throw new InvalidCredentialsException();
    }

    String accessToken = tokenProvider.generateAccessToken(user);
    String refreshToken = tokenProvider.generateRefreshToken(user);
    RefreshTokenHash refreshTokenHash = refreshTokenHasher.hash(refreshToken);

    Instant now = clock.instant();
    Session session = Session.create(SessionId.generate(), refreshTokenHash, user.id(), cmd.deviceId(),
        FamilyId.generate(), null, sessionDuration, now);

    sessionRepository.save(session);
    eventPublisher.publishAll(session.domainEvents());
    session.clearEvents();

    log.info("Login successful: userId={}, sessionId={}", user.id().value(), session.id().value());
    metrics.loginAttempt("success").increment();
    metrics.sessionCreated().increment();

    return new LoginResult(accessToken, refreshToken);
  }
}
