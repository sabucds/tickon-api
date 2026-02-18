package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.auth.infrastructure.persistence.entities.PasswordResetTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {

  public PasswordResetTokenEntity toEntity(PasswordResetToken token) {
    var entity = new PasswordResetTokenEntity();
    entity.id = token.id().value();
    entity.tokenHash = token.tokenHash().value();
    entity.userId = token.userId().value();
    entity.email = token.email().value();
    entity.absoluteExpiresAt = token.absoluteExpiresAt();
    entity.usedAt = token.usedAt();
    return entity;
  }

  public PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
    return PasswordResetToken.restore(ResetTokenId.from(entity.id), ResetTokenHash.from(entity.tokenHash),
        new UserId(entity.userId), Email.from(entity.email), entity.absoluteExpiresAt, entity.usedAt);
  }
}
