package com.tickon.identity.user.application.ports.in;

import com.tickon.identity.user.domain.valueobjects.UserId;

public interface DeleteUserUseCase {
  void handle(UserId userId);
}