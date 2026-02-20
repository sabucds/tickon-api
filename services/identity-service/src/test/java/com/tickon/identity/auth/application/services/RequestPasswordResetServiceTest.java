package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.dto.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.dto.RequestPasswordResetResult;
import com.tickon.identity.auth.application.ports.out.ResetTokenGenerator;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.shared.contracts.queries.GetUserByEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import com.tickon.identity.shared.platform.metrics.IdentityMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

  @Mock
  private QueryBus queryBus;
  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;
  @Mock
  private ResetTokenGenerator resetTokenGenerator;
  @Mock
  private DomainEventPublisher eventPublisher;
  private final IdentityMetrics metrics = new IdentityMetrics(new SimpleMeterRegistry());

  private RequestPasswordResetService service;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration tokenDuration = Duration.ofHours(1);

  @BeforeEach
  void setUp() {
    service = new RequestPasswordResetService(queryBus, resetTokenRepository, resetTokenHasher, resetTokenGenerator,
        tokenDuration.toMillis(), fixedClock, eventPublisher, metrics);
  }

  @Test
  void shouldCreateTokenAndPublishEvent_WhenUserExists() {

    Email email = Email.from("user@example.com");
    UserId userId = new UserId(UUID.randomUUID());
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId.value(), "password-hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("plain-token-abc123");
    when(resetTokenHasher.hash("plain-token-abc123")).thenReturn(ResetTokenHash.from("hashed-token"));

    RequestPasswordResetResult result = service.requestPasswordReset(new RequestPasswordResetCommand(email));

    assertThat(result.message()).contains("If the email exists");

    ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(resetTokenRepository).save(tokenCaptor.capture());
    PasswordResetToken savedToken = tokenCaptor.getValue();
    assertThat(savedToken.email()).isEqualTo(email);
    assertThat(savedToken.userId()).isEqualTo(userId);
    assertThat(savedToken.tokenHash().value()).isEqualTo("hashed-token");

    verify(eventPublisher).publishAll(any());

    verify(resetTokenRepository).invalidateAllForUser(userId, fixedInstant);
  }

  @Test
  void shouldReturnSuccess_WhenUserDoesNotExist() {

    Email email = Email.from("nonexistent@example.com");
    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.empty()));

    RequestPasswordResetResult result = service.requestPasswordReset(new RequestPasswordResetCommand(email));

    assertThat(result.message()).contains("If the email exists");

    verify(resetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldInvalidateOldTokens_BeforeCreatingNew() {

    Email email = Email.from("user@example.com");
    UserId userId = new UserId(UUID.randomUUID());
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId.value(), "hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("token");
    when(resetTokenHasher.hash(any())).thenReturn(ResetTokenHash.from("hash"));

    service.requestPasswordReset(new RequestPasswordResetCommand(email));

    var inOrder = inOrder(resetTokenRepository);
    inOrder.verify(resetTokenRepository).invalidateAllForUser(userId, fixedInstant);
    inOrder.verify(resetTokenRepository).save(any(PasswordResetToken.class));
  }
}
