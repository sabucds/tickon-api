package com.tickon.identity.user.application.ports.in;

public interface DeleteUserUseCase {
  void handle(String userId);
}