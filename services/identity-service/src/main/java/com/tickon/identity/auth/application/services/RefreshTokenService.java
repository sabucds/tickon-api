package com.tickon.identity.auth.application.services;

import com.tickon.common.queries.QueryBus;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.dto.RefreshTokenCommand;
import com.tickon.identity.auth.application.ports.in.RefreshTokenUseCase;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.AuthUser;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidRefreshTokenException;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import com.tickon.identity.contracts.user.queries.GetUserAuthDataQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

  private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

  private final SessionRepository sessionRepository;
  private final QueryBus queryBus;
  private final TokenProvider tokenProvider;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics;

  public RefreshTokenService(SessionRepository sessionRepository, QueryBus queryBus, TokenProvider tokenProvider,
      RefreshTokenHasher refreshTokenHasher, Clock clock, DomainEventPublisher eventPublisher,
      IdentityMetrics metrics) {
    this.sessionRepository = sessionRepository;
    this.queryBus = queryBus;
    this.tokenProvider = tokenProvider;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
    this.metrics = metrics;
  }

  @Override
  @Transactional
  public LoginResult refresh(RefreshTokenCommand command) {
    RefreshTokenHash tokenHash = refreshTokenHasher.hash(command.refreshToken());

    Session session = sessionRepository.findByRefreshTokenHash(tokenHash.value()).orElseThrow(() -> {
      log.warn("Token refresh failed: token not found");
      metrics.tokenRefresh("failure").increment();
      metrics.tokenRefreshFailure("invalid").increment();
      return new InvalidRefreshTokenException();
    });

    Instant now = clock.instant();

    if (session.isRevoked()) {
      sessionRepository.revokeAllByFamilyId(session.familyId(), now, RevokeReason.TOKEN_REUSE_DETECTED);
      log.warn("Token refresh failed: token reuse detected for sessionId={}", session.id().value());
      metrics.tokenRefresh("failure").increment();
      metrics.tokenRefreshFailure("revoked").increment();
      metrics.sessionRevoked("rotation_failure").increment();
      throw new InvalidRefreshTokenException();
    }

    if (session.isExpired(now)) {
      log.warn("Token refresh failed: session expired for sessionId={}", session.id().value());
      metrics.tokenRefresh("failure").increment();
      metrics.tokenRefreshFailure("expired").increment();
      throw new InvalidRefreshTokenException();
    }

    Optional<UserAuthDataDTO> userOpt = queryBus.execute(new GetUserAuthDataQuery(session.userId().value()))
        .orElseThrow();

    if (userOpt.isEmpty()) {
      log.warn("Token refresh failed: user not found for sessionId={}", session.id().value());
      metrics.tokenRefresh("failure").increment();
      metrics.tokenRefreshFailure("invalid").increment();
      throw new InvalidRefreshTokenException();
    }

    AuthUser user = AuthUser.fromDTO(userOpt.get());

    String newAccessToken = tokenProvider.generateAccessToken(user);
    String newRefreshToken = tokenProvider.generateRefreshToken(user);
    RefreshTokenHash newTokenHash = refreshTokenHasher.hash(newRefreshToken);

    Session newSession = session.rotateTo(now, newTokenHash, SessionId.generate());

    sessionRepository.save(session);
    sessionRepository.save(newSession);

    eventPublisher.publishAll(session.domainEvents());
    eventPublisher.publishAll(newSession.domainEvents());
    session.clearEvents();
    newSession.clearEvents();

    log.debug("Token refresh successful: userId={}, newSessionId={}", user.id().value(), newSession.id().value());
    metrics.tokenRefresh("success").increment();
    metrics.sessionCreated().increment();
    metrics.sessionRevoked("rotation").increment();

    return new LoginResult(newAccessToken, newRefreshToken);
  }
}
