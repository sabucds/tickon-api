package com.tickon.identity.auth.application.command.requestpasswordreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.ports.ResetTokenGenerator;
import com.tickon.identity.auth.application.ports.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.contracts.user.queries.GetUserByEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.shared.kernel.ports.DomainEventPublisher;
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
class RequestPasswordResetCommandHandlerTest {

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

  private RequestPasswordResetCommandHandler handler;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration tokenDuration = Duration.ofHours(1);

  @BeforeEach
  void setUp() {
    handler = new RequestPasswordResetCommandHandler(queryBus, resetTokenRepository, resetTokenHasher,
        resetTokenGenerator, tokenDuration.toMillis(), fixedClock, eventPublisher, metrics);
  }

  @Test
  void shouldCreateTokenAndPublishEvent_WhenUserExists() {
    String email = "user@example.com";
    UUID userId = UUID.randomUUID();
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId, "password-hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("plain-token-abc123");
    when(resetTokenHasher.hash("plain-token-abc123")).thenReturn(ResetTokenHash.from("hashed-token"));

    RequestPasswordResetResult result = handler.handle(new RequestPasswordResetCommand(email)).orElseThrow();

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
    String email = "nonexistent@example.com";
    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.empty()));

    RequestPasswordResetResult result = handler.handle(new RequestPasswordResetCommand(email)).orElseThrow();

    assertThat(result.message()).contains("If the email exists");
    verify(resetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldInvalidateOldTokens_BeforeCreatingNew() {
    String email = "user@example.com";
    UUID userId = UUID.randomUUID();
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId, "hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(Optional.of(userDTO)));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("token");
    when(resetTokenHasher.hash(any())).thenReturn(ResetTokenHash.from("hash"));

    handler.handle(new RequestPasswordResetCommand(email));

    var inOrderVerify = inOrder(resetTokenRepository);
    inOrderVerify.verify(resetTokenRepository).invalidateAllForUser(userId, fixedInstant);
    inOrderVerify.verify(resetTokenRepository).save(any(PasswordResetToken.class));
  }
}
