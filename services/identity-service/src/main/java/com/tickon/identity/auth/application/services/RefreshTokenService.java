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
import com.tickon.identity.shared.contracts.queries.GetUserAuthDataQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

  private final SessionRepository sessionRepository;
  private final QueryBus queryBus;
  private final TokenProvider tokenProvider;
  private final RefreshTokenHasher refreshTokenHasher;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;

  public RefreshTokenService(SessionRepository sessionRepository, QueryBus queryBus, TokenProvider tokenProvider,
      RefreshTokenHasher refreshTokenHasher, Clock clock, DomainEventPublisher eventPublisher) {
    this.sessionRepository = sessionRepository;
    this.queryBus = queryBus;
    this.tokenProvider = tokenProvider;
    this.refreshTokenHasher = refreshTokenHasher;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public LoginResult refresh(RefreshTokenCommand command) {
    RefreshTokenHash tokenHash = refreshTokenHasher.hash(command.refreshToken());

    Session session = sessionRepository.findByRefreshTokenHash(tokenHash.value())
        .orElseThrow(InvalidRefreshTokenException::new);

    Instant now = clock.instant();

    if (session.isRevoked()) {
      sessionRepository.revokeAllByFamilyId(session.familyId(), now, RevokeReason.TOKEN_REUSE_DETECTED);
      throw new InvalidRefreshTokenException();
    }

    if (session.isExpired(now)) {
      throw new InvalidRefreshTokenException();
    }

    UserAuthDataDTO userDTO = queryBus.execute(new GetUserAuthDataQuery(session.userId().value())).orElseThrow()
        .orElseThrow(() -> new InvalidRefreshTokenException()); // Unwrap Optional

    AuthUser user = AuthUser.fromDTO(userDTO);

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

    return new LoginResult(newAccessToken, newRefreshToken);
  }
}
