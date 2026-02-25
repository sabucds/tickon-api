package com.tickon.identity.user.application.query.getuserbyid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.ports.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.shared.UserTestFixtures;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserByIdQueryHandlerTest {

  private GetUserByIdQueryHandler handler;

  @Mock
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    handler = new GetUserByIdQueryHandler(userRepository);
  }

  @Test
  void shouldGetUserById_WhenUserExists() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    User user = UserTestFixtures.aUserWithId(userId);
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.of(user));
    Optional<UserResult> result = handler.handle(new GetUserByIdQuery(UserId.from(userId))).orElseThrow();
    assertThat(result).isPresent();
    assertThat(result.get().username()).isEqualTo(user.username().value());
  }

  @Test
  void shouldReturnEmpty_WhenUserDoesNotExist() {
    String userId = "123e4567-e89b-12d3-a456-426614174999";
    when(userRepository.findById(UserId.from(userId))).thenReturn(Optional.empty());
    Optional<UserResult> result = handler.handle(new GetUserByIdQuery(UserId.from(userId))).orElseThrow();
    assertThat(result).isNotPresent();
  }
}
