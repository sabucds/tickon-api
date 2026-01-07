package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;

@ExtendWith(MockitoExtension.class)
class GetUserByIdServiceTest {

  private GetUserByIdService getUserByIdService;

  @Mock
  private UserRepository userRepository;

  private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    getUserByIdService = new GetUserByIdService(userRepository);
  }

  @Test
  void shouldGetUserById_WhenUserExists() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    User user = User.create(UserId.from(userId), Email.from("john@email.com"), Username.from("john_doe"), "John", "Doe",
        new PasswordHash("hashed-password"), FIXED_INSTANT);
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.of(user));
    Optional<UserResult> result = getUserByIdService.handle(userId);
    assertThat(result).isPresent();
    assertThat(result.get().username()).isEqualTo("john_doe");
  }

  @Test
  void shouldReturnEmpty_WhenUserDoesNotExist() {
    String userId = "123e4567-e89b-12d3-a456-426614174999";
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.empty());
    Optional<UserResult> result = getUserByIdService.handle(userId);
    assertThat(result).isNotPresent();
  }

}
