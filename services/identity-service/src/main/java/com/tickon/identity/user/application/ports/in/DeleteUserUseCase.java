package com.tickon.identity.user.application.ports.in;

import com.tickon.common.identity.domain.valueobjects.UserId;

public interface DeleteUserUseCase {
  void handle(UserId userId);
}