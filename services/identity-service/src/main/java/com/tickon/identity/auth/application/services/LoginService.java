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
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

  private final QueryBus queryBus;
  private final SessionRepository sessionRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final Clock clock;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Duration sessionDuration;
  private final DomainEventPublisher eventPublisher;

  public LoginService(QueryBus queryBus, SessionRepository sessionRepository, PasswordHasher passwordHasher,
      TokenProvider tokenProvider, @Value("${security.jwt.session-expiration-ms}") long sessionExpirationMs,
      java.time.Clock clock, RefreshTokenHasher refreshTokenHasher, DomainEventPublisher eventPublisher) {
    this.queryBus = queryBus;
    this.sessionRepository = sessionRepository;
    this.passwordHasher = passwordHasher;
    this.tokenProvider = tokenProvider;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.sessionDuration = Duration.ofMillis(sessionExpirationMs);
    this.eventPublisher = eventPublisher;
  }

  @Override
  public LoginResult login(LoginCommand cmd) {
    UserAuthDataDTO userDTO = queryBus.execute(new GetUserByUsernameOrEmailQuery(cmd.usernameOrEmail())).orElseThrow()
        .orElseThrow(() -> new InvalidCredentialsException());

    AuthUser user = AuthUser.fromDTO(userDTO);

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
    eventPublisher.publishAll(session.domainEvents());
    session.clearEvents();

    return new LoginResult(accessToken, refreshToken);
  }
}
