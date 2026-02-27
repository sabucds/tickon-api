package com.tickon.identity.auth.application.query.verifyresettoken;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.ports.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VerifyResetTokenQueryHandler implements QueryHandler<VerifyResetTokenQuery, VerifyResetTokenResult> {

  private static final Logger log = LoggerFactory.getLogger(VerifyResetTokenQueryHandler.class);

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final Clock clock;

  public VerifyResetTokenQueryHandler(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher,
      Clock clock) {
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.clock = clock;
  }

  @Override
  public QueryResult<VerifyResetTokenResult> handle(VerifyResetTokenQuery query) {
    ResetTokenHash tokenHash = resetTokenHasher.hash(query.resetToken());

    return new QueryResult.Success<>(resetTokenRepository.findByTokenHash(tokenHash.value()).map(token -> {
      Instant now = clock.instant();
      boolean valid = !token.isExpired(now) && !token.isUsed();
      log.debug("Reset token verification: valid={}", valid);
      return new VerifyResetTokenResult(valid);
    }).orElseGet(() -> {
      log.debug("Reset token verification: token not found");
      return new VerifyResetTokenResult(false);
    }));
  }

  @Override
  public Class<VerifyResetTokenQuery> getQueryClass() {
    return VerifyResetTokenQuery.class;
  }
}
