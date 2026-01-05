package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import com.tickon.identity.user.domain.valueobjects.PasswordHash;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.domain.valueobjects.Username;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private SessionRepository sessionRepository;

  @Mock
  private PasswordHasher passwordHasher;

  @Mock
  private TokenProvider tokenProvider;

  private LoginService loginService;

  private Clock clock = Clock.systemUTC();

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");

  @BeforeEach
  void setUp() {
    loginService = new LoginService(userRepository, sessionRepository, passwordHasher, tokenProvider,
        Duration.ofHours(1), clock);

  }

  @Test
  void shouldLoginAndPersistSession_WhenCredentialsAreValid() {
    User user = User.create(UserId.generate(), Email.from("john@example.com"), Username.from("johnny_doe"), "John",
        "Doe", new PasswordHash("hashed-password"), fixedInstant);

    when(userRepository.findByUsernameOrEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordHasher.verify("plain-password", user.passwordHash())).thenReturn(true);
    when(tokenProvider.generateAccessToken(user)).thenReturn("access-token");
    when(tokenProvider.generateRefreshToken(user)).thenReturn("refresh-token");

    LoginResult result = loginService.login(new LoginCommand("john@example.com", "plain-password"));

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());

    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.userId()).isEqualTo(user.id());
    assertThat(savedSession.refreshToken()).isEqualTo("refresh-token");
    assertThat(savedSession.isValid()).isTrue();
    assertThat(savedSession.expiresAt()).isAfter(Instant.now());
  }

  @Test
  void shouldThrow_WhenUserNotFound() {
    when(userRepository.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> loginService.login(new LoginCommand("missing", "any")))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenPasswordIsInvalid() {
    User user = User.create(UserId.generate(), Email.from("john@example.com"), Username.from("johnny_doe"), "John",
        "Doe", new PasswordHash("hashed-password"), fixedInstant);

    when(userRepository.findByUsernameOrEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordHasher.verify("wrong-password", user.passwordHash())).thenReturn(false);

    assertThatThrownBy(() -> loginService.login(new LoginCommand("john@example.com", "wrong-password")))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }
}
