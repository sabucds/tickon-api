package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.shared.UserTestFixtures;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteUserServiceTest {

  private DeleteUserService deleteUserService;

  @Mock
  private UserRepository userRepository;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  @BeforeEach
  void setUp() {
    deleteUserService = new DeleteUserService(userRepository, metrics);
  }

  @Test
  void shouldDeleteUserById_WhenUserExists() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    User user = UserTestFixtures.aUserWithId(userId);
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.of(user));
    deleteUserService.handle(UserId.from(userId));
  }

  @Test
  void shouldThrowException_WhenUserDoesNotExist() {
    String userId = "123e4567-e89b-12d3-a456-426614174999";
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.empty());
    try {
      deleteUserService.handle(UserId.from(userId));
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("User not found with id: " + UserId.from(userId));
    }
  }

}
