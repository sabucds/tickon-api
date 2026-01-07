package com.tickon.identity.auth.application.ports.out;

import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.valueobjects.SessionId;
import java.util.Optional;

public interface SessionRepository {

  void save(Session session);

  Optional<Session> findById(SessionId id);

  Optional<Session> findByRefreshTokenHash(String refreshToken);
}