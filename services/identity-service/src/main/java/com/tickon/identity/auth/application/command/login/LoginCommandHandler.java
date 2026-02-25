package com.tickon.identity.auth.application.command.login;

import com.tickon.common.commands.CommandHandler;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.application.ports.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.SessionRepository;
import com.tickon.identity.auth.application.ports.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.FamilyId;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.contracts.user.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
import com.tickon.identity.shared.kernel.ports.PasswordHasher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginCommandHandler implements CommandHandler<LoginCommand, LoginResult> {

  private static final Logger log = LoggerFactory.getLogger(LoginCommandHandler.class);

  private final QueryBus queryBus;
  private final SessionRepository sessionRepository;
  private final PasswordHasher passwordHasher;
  private final TokenProvider tokenProvider;
  private final Clock clock;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Duration sessionDuration;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public LoginCommandHandler(QueryBus queryBus, SessionRepository sessionRepository, PasswordHasher passwordHasher,
      TokenProvider tokenProvider, @Value("${security.jwt.session-expiration-ms}") long sessionExpirationMs,
      Clock clock, RefreshTokenHasher refreshTokenHasher, DomainEventPublisher eventPublisher,
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
  @Transactional
  public CommandResult<LoginResult> handle(LoginCommand cmd) {
    log.info("Login attempt for user '{}'", cmd.usernameOrEmail());

    Optional<UserAuthDataDTO> userOpt = queryBus.execute(new GetUserByUsernameOrEmailQuery(cmd.usernameOrEmail()))
        .orElseThrow();

    if (userOpt.isEmpty()) {
      log.warn("Login failed: user not found for '{}'", cmd.usernameOrEmail());
      metrics.loginAttempt("failure").increment();
      metrics.loginFailure("user_not_found").increment();
      throw new InvalidCredentialsException();
    }

    UserAuthDataDTO user = userOpt.get();
    UUID userId = user.id();
    String passwordHash = user.passwordHash();

    if (!passwordHasher.verify(cmd.password(), passwordHash)) {
      log.warn("Login failed: invalid credentials for '{}'", cmd.usernameOrEmail());
      metrics.loginAttempt("failure").increment();
      metrics.loginFailure("invalid_credentials").increment();
      throw new InvalidCredentialsException();
    }

    String accessToken = tokenProvider.generateAccessToken(userId);
    String refreshToken = tokenProvider.generateRefreshToken();
    RefreshTokenHash refreshTokenHash = refreshTokenHasher.hash(refreshToken);

    Instant now = clock.instant();
    Session session = Session.create(SessionId.generate(), refreshTokenHash, userId, cmd.deviceId(),
        FamilyId.generate(), null, sessionDuration, now);

    sessionRepository.save(session);
    eventPublisher.publishAll(session.domainEvents());
    session.clearEvents();

    log.info("Login successful: userId={}, sessionId={}", userId, session.id().value());
    metrics.loginAttempt("success").increment();
    metrics.sessionCreated().increment();

    return new CommandResult.Success<>(new LoginResult(accessToken, refreshToken));
  }

  @Override
  public Class<LoginCommand> getCommandClass() {
    return LoginCommand.class;
  }
}
