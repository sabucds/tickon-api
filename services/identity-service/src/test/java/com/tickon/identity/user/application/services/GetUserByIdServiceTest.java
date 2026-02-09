package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.application.dto.UserResult;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.shared.UserTestFixtures;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserByIdServiceTest {

  private GetUserByIdService getUserByIdService;

  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    getUserByIdService = new GetUserByIdService(userRepository);
  }

  @Test
  void shouldGetUserById_WhenUserExists() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    User user = UserTestFixtures.aUserWithId(userId);
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.of(user));
    Optional<UserResult> result = getUserByIdService.handle(UserId.from(userId));
    assertThat(result).isPresent();
    assertThat(result.get().username()).isEqualTo(user.username().value());
  }

  @Test
  void shouldReturnEmpty_WhenUserDoesNotExist() {
    String userId = "123e4567-e89b-12d3-a456-426614174999";
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.empty());
    Optional<UserResult> result = getUserByIdService.handle(UserId.from(userId));
    assertThat(result).isNotPresent();
  }

}
