package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.identity.auth.application.dto.LoginCommand;
import com.tickon.identity.auth.application.dto.LoginResult;
import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.auth.domain.Session;
import com.tickon.identity.auth.domain.exceptions.InvalidCredentialsException;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;
import com.tickon.identity.auth.shared.AuthTestFixtures;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
  @Mock
  private RefreshTokenHasher refreshTokenHasher;

  private LoginService loginService;

  private final Instant fixedInstant = Instant.parse("2024-01-01T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration sessionTtl = Duration.ofHours(1);

  private static final String IDENTIFIER = "john@example.com";
  private static final String DEVICE_ID = "device-123";

  @BeforeEach
  void setUp() {
    loginService = new LoginService(userRepository, sessionRepository, passwordHasher, tokenProvider, sessionTtl,
        fixedClock, refreshTokenHasher);
  }

  @Test
  void shouldLoginAndPersistSession_WhenCredentialsAreValid() {
    User user = AuthTestFixtures.aUser();
    stubUserFound(user);
    stubValidPassword("plain-password", user);
    stubTokens(user, "access-token", "refresh-token", "hashed-refresh-token");

    LoginResult result = loginService.login(new LoginCommand(IDENTIFIER, "plain-password", DEVICE_ID));

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");

    ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
    verify(sessionRepository).save(sessionCaptor.capture());

    Session savedSession = sessionCaptor.getValue();
    assertThat(savedSession.userId()).isEqualTo(user.id());
    assertThat(savedSession.deviceId()).isEqualTo(DEVICE_ID);
    assertThat(savedSession.refreshTokenHash()).isEqualTo(RefreshTokenHash.from("hashed-refresh-token"));
    assertThat(savedSession.isRevoked()).isFalse();
    assertThat(savedSession.expiresAt()).isEqualTo(fixedInstant.plus(sessionTtl));
  }

  @Test
  void shouldThrow_WhenUserNotFound() {
    when(userRepository.findByUsernameOrEmail("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> loginService.login(new LoginCommand("missing", "any", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }

  @Test
  void shouldThrow_WhenPasswordIsInvalid() {
    User user = AuthTestFixtures.aUser();
    stubUserFound(user);
    stubInvalidPassword("wrong-password", user);

    assertThatThrownBy(() -> loginService.login(new LoginCommand(IDENTIFIER, "wrong-password", DEVICE_ID)))
        .isInstanceOf(InvalidCredentialsException.class).hasMessageContaining("Invalid credentials");

    verify(sessionRepository, never()).save(any());
  }

  private void stubUserFound(User user) {
    when(userRepository.findByUsernameOrEmail(IDENTIFIER)).thenReturn(Optional.of(user));
  }

  private void stubValidPassword(String raw, User user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(true);
  }

  private void stubInvalidPassword(String raw, User user) {
    when(passwordHasher.verify(raw, user.passwordHash())).thenReturn(false);
  }

  private void stubTokens(User user, String access, String refresh, String refreshHash) {
    when(tokenProvider.generateAccessToken(user)).thenReturn(access);
    when(tokenProvider.generateRefreshToken(user)).thenReturn(refresh);
    when(refreshTokenHasher.hash(refresh)).thenReturn(RefreshTokenHash.from(refreshHash));
  }
}
