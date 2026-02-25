package com.tickon.identity.user.application.command.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import com.tickon.identity.user.application.ports.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.shared.UserTestFixtures;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteUserCommandHandlerTest {

  private DeleteUserCommandHandler handler;

  @Mock
  private UserRepository userRepository;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  @BeforeEach
  void setUp() {
    handler = new DeleteUserCommandHandler(userRepository, metrics);
  }

  @Test
  void shouldDeleteUserById_WhenUserExists() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    UserId id = UserId.from(userId);
    User user = UserTestFixtures.aUserWithId(userId);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    handler.handle(new DeleteUserCommand(id));

    verify(userRepository).delete(id);
    assertThat(metrics.userDeleted().count()).isEqualTo(1.0);
  }

  @Test
  void shouldThrowException_WhenUserDoesNotExist() {
    String userId = "123e4567-e89b-12d3-a456-426614174999";
    UserId id = UserId.from(userId);
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.handle(new DeleteUserCommand(id))).isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User not found with id: " + id);
  }
}
