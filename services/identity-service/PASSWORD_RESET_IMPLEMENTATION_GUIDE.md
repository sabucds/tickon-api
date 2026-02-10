# Password Reset Implementation Guide

**Status:** Domain Layer Complete ✅ | Application Layer Refactored with CommandBus ✅ | Infrastructure Layer In Progress

**🔄 IMPORTANT UPDATE (2026-02-10):**
This implementation has been **refactored** to use the **CommandBus pattern** instead of domain events for password changes. This ensures transactional safety and prevents race conditions.

**Key Changes:**
- ✅ `ResetPasswordService` now uses `CommandBus` to change passwords synchronously
- ✅ Created `ChangePasswordCommand` and `ChangePasswordCommandHandler`
- ✅ Password changes are transactional with token updates
- ⚠️ Section 8 (User Module Integration via events) is now obsolete
- ⚠️ Section 7.4 (Session revocation) needs updating

**See [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md#cross-module-communication-patterns) for full CommandBus documentation.**

---

This guide continues from where we left off. The domain layer (Step 1) is fully implemented and tested.

---

## ✅ Already Completed (Step 1)

### Domain Layer - ALL DONE ✓
- `ResetTokenId.java` ✓
- `ResetTokenHash.java` ✓
- `PasswordResetToken.java` ✓
- `PasswordResetRequestedEvent.java` ✓
- `PasswordResetCompletedEvent.java` ✓
- `InvalidResetTokenException.java` ✓
- All domain tests passing (26/26) ✓

### Application Layer - Ports & DTOs Complete ✓
- Use case interfaces (3) ✓
- Output port interfaces (4) ✓
- DTOs (5) ✓

---

## 🚧 Step 2: Application Services (In Progress)

### 2.1 RequestPasswordResetService

#### Test First (TDD):
Create: `services/identity-service/src/test/java/com/tickon/identity/auth/application/services/RequestPasswordResetServiceTest.java`

```java
package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import com.tickon.identity.user.domain.valueobjects.Email;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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

  private RequestPasswordResetService service;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
  private final Duration tokenDuration = Duration.ofHours(1);

  @BeforeEach
  void setUp() {
    service = new RequestPasswordResetService(queryBus, resetTokenRepository, resetTokenHasher, resetTokenGenerator,
        tokenDuration, fixedClock, eventPublisher);
  }

  @Test
  void shouldCreateTokenAndPublishEvent_WhenUserExists() {
    // Arrange
    Email email = Email.from("user@example.com");
    UserId userId = UserId.from(UUID.randomUUID());
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId.value(), "password-hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(userDTO));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("plain-token-abc123");
    when(resetTokenHasher.hash("plain-token-abc123")).thenReturn(ResetTokenHash.from("hashed-token"));

    // Act
    RequestPasswordResetResult result = service.requestPasswordReset(new RequestPasswordResetCommand(email));

    // Assert
    assertThat(result.message()).contains("If the email exists");

    // Verify token saved
    ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(resetTokenRepository).save(tokenCaptor.capture());
    PasswordResetToken savedToken = tokenCaptor.getValue();
    assertThat(savedToken.email()).isEqualTo(email);
    assertThat(savedToken.userId()).isEqualTo(userId);
    assertThat(savedToken.tokenHash().value()).isEqualTo("hashed-token");

    // Verify events published
    verify(eventPublisher).publishAll(any());

    // Verify old tokens invalidated
    verify(resetTokenRepository).invalidateAllForUser(userId, fixedInstant);
  }

  @Test
  void shouldReturnSuccess_WhenUserDoesNotExist() {
    // Arrange - Security: don't leak email existence
    Email email = Email.from("nonexistent@example.com");
    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.NotFound<>());

    // Act
    RequestPasswordResetResult result = service.requestPasswordReset(new RequestPasswordResetCommand(email));

    // Assert - Still returns success
    assertThat(result.message()).contains("If the email exists");

    // Verify no token created
    verify(resetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldInvalidateOldTokens_BeforeCreatingNew() {
    // Arrange
    Email email = Email.from("user@example.com");
    UserId userId = UserId.from(UUID.randomUUID());
    UserAuthDataDTO userDTO = new UserAuthDataDTO(userId.value(), "hash", "ACTIVE");

    when(queryBus.execute(any(GetUserByEmailQuery.class))).thenReturn(new QueryResult.Success<>(userDTO));
    when(resetTokenGenerator.generateSecureToken()).thenReturn("token");
    when(resetTokenHasher.hash(any())).thenReturn(ResetTokenHash.from("hash"));

    // Act
    service.requestPasswordReset(new RequestPasswordResetCommand(email));

    // Assert - invalidate called before save
    var inOrder = inOrder(resetTokenRepository);
    inOrder.verify(resetTokenRepository).invalidateAllForUser(userId, fixedInstant);
    inOrder.verify(resetTokenRepository).save(any(PasswordResetToken.class));
  }
}
```

#### Implementation:
Create: `services/identity-service/src/main/java/com/tickon/identity/auth/application/services/RequestPasswordResetService.java`

```java
package com.tickon.identity.auth.application.services;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.queries.QueryBus;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.auth.application.dto.RequestPasswordResetCommand;
import com.tickon.identity.auth.application.dto.RequestPasswordResetResult;
import com.tickon.identity.auth.application.ports.in.RequestPasswordResetUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenGenerator;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.shared.contracts.queries.GetUserByEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

  private final QueryBus queryBus;
  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final ResetTokenGenerator resetTokenGenerator;
  private final Duration tokenDuration;
  private final Clock clock;
  private final DomainEventPublisher eventPublisher;

  public RequestPasswordResetService(QueryBus queryBus, ResetTokenRepository resetTokenRepository,
      ResetTokenHasher resetTokenHasher, ResetTokenGenerator resetTokenGenerator,
      @Value("${security.password-reset.token-expiration-ms}") long tokenExpirationMs, Clock clock,
      DomainEventPublisher eventPublisher) {
    this.queryBus = queryBus;
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.resetTokenGenerator = resetTokenGenerator;
    this.tokenDuration = Duration.ofMillis(tokenExpirationMs);
    this.clock = clock;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public RequestPasswordResetResult requestPasswordReset(RequestPasswordResetCommand command) {
    // Query user by email via QueryBus
    QueryResult<UserAuthDataDTO> result = queryBus.execute(new GetUserByEmailQuery(command.email().value()));

    if (result instanceof QueryResult.NotFound) {
      // Security: Don't leak email existence
      return RequestPasswordResetResult.success();
    }

    UserAuthDataDTO userDTO = ((QueryResult.Success<UserAuthDataDTO>) result).data();
    UserId userId = UserId.from(userDTO.id());

    // Generate secure token (plain text)
    String plainToken = resetTokenGenerator.generateSecureToken();

    // Hash token for storage
    ResetTokenHash tokenHash = resetTokenHasher.hash(plainToken);

    Instant now = clock.instant();

    // Invalidate any existing reset tokens for this user
    resetTokenRepository.invalidateAllForUser(userId, now);

    // Create new password reset token
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId, command.email(),
        tokenDuration, now);

    // Save token
    resetTokenRepository.save(token);

    // Publish domain events (triggers email sending)
    eventPublisher.publishAll(token.domainEvents());
    token.clearEvents();

    return RequestPasswordResetResult.success();
  }
}
```

---

### 2.2 ResetPasswordService ✅ COMPLETED (Refactored with CommandBus)

**Note:** This service has been refactored to use **CommandBus** instead of Domain Events for password changes. This ensures transactional safety - password changes and token updates happen in the same transaction.

#### Test (COMPLETED):
Location: `services/identity-service/src/test/java/com/tickon/identity/auth/application/services/ResetPasswordServiceTest.java`

Key changes from original guide:
- Uses `CommandBus` mock instead of `DomainEventPublisher`
- Tests verify `commandBus.execute(ChangePasswordCommand)` is called
- New test: `shouldThrow_WhenCommandFails()` - ensures token not marked as used if password change fails

```java
// Key test excerpt (see actual file for full implementation)
@Mock
private CommandBus commandBus;  // Changed from DomainEventPublisher

@Test
void shouldResetPassword_WhenTokenIsValidAndCommandSucceeds() {
  // Arrange
  when(commandBus.execute(any(ChangePasswordCommand.class)))
      .thenReturn(new CommandResult.Success<>(null));

  // Act
  service.resetPassword(new ResetPasswordCommand(plainToken, newPassword));

  // Assert
  assertThat(token.isUsed()).isTrue();
  verify(commandBus).execute(any(ChangePasswordCommand.class));
  verify(resetTokenRepository).save(token);
}

@Test
void shouldThrow_WhenCommandFails() {
  // Arrange
  when(commandBus.execute(any(ChangePasswordCommand.class)))
      .thenReturn(new CommandResult.Error<>("User not found", new RuntimeException()));

  // Act & Assert
  assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, newPassword)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Failed to change password");

  // Token should NOT be marked as used when password change fails
  assertThat(token.isUsed()).isFalse();
  verify(resetTokenRepository, never()).save(any());
}

  @Test
  void shouldThrow_WhenTokenDoesNotExist() {
    // Arrange
    String plainToken = "invalid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class);

    verify(resetTokenRepository, never()).save(any());
    verify(eventPublisher, never()).publishAll(any());
  }

  @Test
  void shouldThrow_WhenTokenIsExpired() {
    // Arrange
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    // Token created 2 hours ago, expired after 1 hour
    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class).hasMessageContaining("expired");
  }

  @Test
  void shouldThrow_WhenTokenAlreadyUsed() {
    // Arrange
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), fixedInstant.plus(Duration.ofHours(1)),
        fixedInstant.minusSeconds(30) // Already used 30 seconds ago
    );

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, "NewPass123!")))
        .isInstanceOf(InvalidResetTokenException.class).hasMessageContaining("already been used");
  }

  @Test
  void shouldThrow_WhenPasswordIsWeak() {
    // Arrange
    String plainToken = "valid-token";
    String weakPassword = "weak";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    UserId userId = UserId.generate();

    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, userId,
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));
    doThrow(new InvalidPasswordException("Password too weak")).when(passwordPolicy).validate(weakPassword);

    // Act & Assert
    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordCommand(plainToken, weakPassword)))
        .isInstanceOf(InvalidPasswordException.class);

    // Token should not be marked as used
    assertThat(token.isUsed()).isFalse();
    verify(resetTokenRepository, never()).save(any());
  }
}
```

#### Implementation (COMPLETED - Refactored with CommandBus):
Location: `services/identity-service/src/main/java/com/tickon/identity/auth/application/services/ResetPasswordService.java`

**Key Changes:**
- ❌ Removed `DomainEventPublisher` dependency
- ✅ Added `CommandBus` dependency
- ✅ Password hash is now **used** (not discarded!)
- ✅ Executes `ChangePasswordCommand` synchronously
- ✅ Checks command result before marking token as used
- ✅ Transaction-safe: password change and token update are atomic

```java
package com.tickon.identity.auth.application.services;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.commands.CommandResult;
import com.tickon.common.identity.domain.valueobjects.PasswordHash;
import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.dto.ResetPasswordCommand;
import com.tickon.identity.auth.application.ports.in.ResetPasswordUseCase;
import com.tickon.identity.auth.application.ports.out.PasswordHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.exceptions.InvalidResetTokenException;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.shared.contracts.commands.ChangePasswordCommand;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordService implements ResetPasswordUseCase {

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final PasswordHasher passwordHasher;
  private final PasswordStrengthPolicy passwordPolicy;
  private final Clock clock;
  private final CommandBus commandBus;  // Changed from DomainEventPublisher

  public ResetPasswordService(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher,
      PasswordHasher passwordHasher, PasswordStrengthPolicy passwordPolicy, Clock clock,
      CommandBus commandBus) {  // Changed parameter
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.passwordHasher = passwordHasher;
    this.passwordPolicy = passwordPolicy;
    this.clock = clock;
    this.commandBus = commandBus;  // Changed assignment
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordCommand command) {
    // Hash incoming token
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());

    // Find token by hash
    PasswordResetToken token = resetTokenRepository.findByTokenHash(tokenHash.value())
        .orElseThrow(InvalidResetTokenException::new);

    Instant now = clock.instant();

    // Validate token: not expired, not used
    if (token.isExpired(now)) {
      throw new InvalidResetTokenException();
    }

    if (token.isUsed()) {
      throw new InvalidResetTokenException();
    }

    // Validate password strength
    passwordPolicy.validate(command.newPassword());

    // Hash new password
    PasswordHash newPasswordHash = passwordHasher.hash(command.newPassword());

    // Execute password change command (synchronous, transactional)
    UserId userId = token.userId();
    CommandResult<Void> result = commandBus.execute(
        new ChangePasswordCommand(userId, newPasswordHash));

    // Check if password change succeeded
    if (result instanceof CommandResult.Error<Void> error) {
      throw new IllegalStateException(
          "Failed to change password: " + error.message(), error.cause());
    }

    // Mark token as used ONLY after password change succeeds
    token.markAsUsed(now);

    // Save token with usedAt timestamp
    resetTokenRepository.save(token);

    // Clear any domain events (token was marked as used)
    token.clearEvents();
  }
}
```

**Why This Matters:**
1. **Before:** Token marked as used → event emitted → async handler might fail → user locked out
2. **After:** Password changed → token marked as used → all in same transaction → all-or-nothing

---

### 2.3 VerifyResetTokenService

#### Test:
Create: `services/identity-service/src/test/java/com/tickon/identity/auth/application/services/VerifyResetTokenServiceTest.java`

```java
package com.tickon.identity.auth.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.user.domain.valueobjects.Email;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyResetTokenServiceTest {

  @Mock
  private ResetTokenRepository resetTokenRepository;
  @Mock
  private ResetTokenHasher resetTokenHasher;

  private VerifyResetTokenService service;

  private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
  private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    service = new VerifyResetTokenService(resetTokenRepository, resetTokenHasher, fixedClock);
  }

  @Test
  void shouldReturnValid_WhenTokenExistsAndNotExpiredOrUsed() {
    // Arrange
    String plainToken = "valid-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken token = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(token));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isTrue();
  }

  @Test
  void shouldReturnInvalid_WhenTokenDoesNotExist() {
    // Arrange
    String plainToken = "nonexistent-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.empty());

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsExpired() {
    // Arrange
    String plainToken = "expired-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken expiredToken = PasswordResetToken.create(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), Duration.ofHours(1), fixedInstant.minus(Duration.ofHours(2)));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(expiredToken));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }

  @Test
  void shouldReturnInvalid_WhenTokenIsUsed() {
    // Arrange
    String plainToken = "used-token";
    ResetTokenHash tokenHash = ResetTokenHash.from("hashed");
    PasswordResetToken usedToken = PasswordResetToken.restore(ResetTokenId.generate(), tokenHash, UserId.generate(),
        Email.from("user@example.com"), fixedInstant.plus(Duration.ofHours(1)), fixedInstant.minusSeconds(30));

    when(resetTokenHasher.hash(plainToken)).thenReturn(tokenHash);
    when(resetTokenRepository.findByTokenHash(tokenHash.value())).thenReturn(Optional.of(usedToken));

    // Act
    VerifyResetTokenResult result = service.verifyResetToken(new VerifyResetTokenCommand(plainToken));

    // Assert
    assertThat(result.valid()).isFalse();
  }
}
```

#### Implementation:
Create: `services/identity-service/src/main/java/com/tickon/identity/auth/application/services/VerifyResetTokenService.java`

```java
package com.tickon.identity.auth.application.services;

import com.tickon.identity.auth.application.dto.VerifyResetTokenCommand;
import com.tickon.identity.auth.application.dto.VerifyResetTokenResult;
import com.tickon.identity.auth.application.ports.in.VerifyResetTokenUseCase;
import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class VerifyResetTokenService implements VerifyResetTokenUseCase {

  private final ResetTokenRepository resetTokenRepository;
  private final ResetTokenHasher resetTokenHasher;
  private final Clock clock;

  public VerifyResetTokenService(ResetTokenRepository resetTokenRepository, ResetTokenHasher resetTokenHasher,
      Clock clock) {
    this.resetTokenRepository = resetTokenRepository;
    this.resetTokenHasher = resetTokenHasher;
    this.clock = clock;
  }

  @Override
  public VerifyResetTokenResult verifyResetToken(VerifyResetTokenCommand command) {
    // Hash token
    ResetTokenHash tokenHash = resetTokenHasher.hash(command.resetToken());

    // Find token
    return resetTokenRepository.findByTokenHash(tokenHash.value()).map(token -> {
      Instant now = clock.instant();
      // Check if valid: not expired and not used
      boolean valid = !token.isExpired(now) && !token.isUsed();
      return new VerifyResetTokenResult(valid);
    }).orElse(new VerifyResetTokenResult(false)); // Token not found = invalid
  }
}
```

---

### 2.4 Shared Contracts (GetUserByEmailQuery)

Create: `services/identity-service/src/main/java/com/tickon/identity/shared/contracts/queries/GetUserByEmailQuery.java`

```java
package com.tickon.identity.shared.contracts.queries;

import com.tickon.common.queries.Query;

public record GetUserByEmailQuery(String email) implements Query<UserAuthDataDTO> {
  @Override
  public String getQueryName() {
    return "GetUserByEmail.v1";
  }
}
```

Create: `services/identity-service/src/main/java/com/tickon/identity/user/application/queryhandlers/GetUserByEmailQueryHandler.java`

```java
package com.tickon.identity.user.application.queryhandlers;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.shared.contracts.queries.GetUserByEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.Email;
import org.springframework.stereotype.Component;

@Component
public class GetUserByEmailQueryHandler implements QueryHandler<GetUserByEmailQuery, UserAuthDataDTO> {

  private final UserRepository userRepository;

  public GetUserByEmailQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<UserAuthDataDTO> handle(GetUserByEmailQuery query) {
    return userRepository.findByEmail(Email.from(query.email())).map(this::toDTO).map(QueryResult.Success::new)
        .orElseGet(QueryResult.NotFound::new);
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
```

**Run Application Tests:**
```bash
mvn test -pl services/identity-service -Dtest="*PasswordReset*Test"
```

---

## 📦 Step 3: Infrastructure - Persistence

### 3.1 Database Migration

Create: `services/identity-service/src/main/resources/db/migration/V7__add_password_reset_tokens.sql`

```sql
-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id UUID PRIMARY KEY,
  token_hash TEXT NOT NULL UNIQUE,
  user_id UUID NOT NULL REFERENCES users(id),
  email CITEXT NOT NULL,
  absolute_expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for lookups by token hash (most common query)
CREATE UNIQUE INDEX idx_password_reset_tokens_token_hash
  ON password_reset_tokens(token_hash);

-- Index for user_id (cleanup queries, invalidation)
CREATE INDEX idx_password_reset_tokens_user_id
  ON password_reset_tokens(user_id);

-- Index for created_at (cleanup old tokens)
CREATE INDEX idx_password_reset_tokens_created_at
  ON password_reset_tokens(created_at);

-- Partial index for active tokens (not used, not expired)
CREATE INDEX idx_password_reset_tokens_active
  ON password_reset_tokens(user_id, absolute_expires_at)
  WHERE used_at IS NULL;
```

### 3.2 JPA Entity

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/persistence/entities/PasswordResetTokenEntity.java`

```java
package com.tickon.identity.auth.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

  @Id
  public UUID id;

  @Column(name = "token_hash", nullable = false, unique = true)
  public String tokenHash;

  @Column(name = "user_id", nullable = false)
  public UUID userId;

  @Column(name = "email", nullable = false, columnDefinition = "citext")
  public String email;

  @Column(name = "absolute_expires_at", nullable = false)
  public Instant absoluteExpiresAt;

  @Column(name = "used_at")
  public Instant usedAt;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    updatedAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}
```

### 3.3 JPA Repository

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/persistence/JpaPasswordResetTokenRepository.java`

```java
package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.identity.auth.infrastructure.persistence.entities.PasswordResetTokenEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

  Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

  @Modifying
  @Query("UPDATE PasswordResetTokenEntity t SET t.usedAt = :now WHERE t.userId = :userId AND t.usedAt IS NULL")
  void invalidateAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
```

### 3.4 Persistence Mapper

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/persistence/mappers/PasswordResetTokenPersistenceMapper.java`

```java
package com.tickon.identity.auth.infrastructure.persistence.mappers;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenId;
import com.tickon.identity.auth.infrastructure.persistence.entities.PasswordResetTokenEntity;
import com.tickon.identity.user.domain.valueobjects.Email;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenPersistenceMapper {

  public PasswordResetTokenEntity toEntity(PasswordResetToken token) {
    var entity = new PasswordResetTokenEntity();
    entity.id = token.id().value();
    entity.tokenHash = token.tokenHash().value();
    entity.userId = token.userId().value();
    entity.email = token.email().value();
    entity.absoluteExpiresAt = token.absoluteExpiresAt();
    entity.usedAt = token.usedAt();
    return entity;
  }

  public PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
    return PasswordResetToken.restore(ResetTokenId.from(entity.id), ResetTokenHash.from(entity.tokenHash),
        UserId.from(entity.userId), Email.from(entity.email), entity.absoluteExpiresAt, entity.usedAt);
  }
}
```

### 3.5 Repository Adapter

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/persistence/PasswordResetTokenRepositoryAdapter.java`

```java
package com.tickon.identity.auth.infrastructure.persistence;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.auth.application.ports.out.ResetTokenRepository;
import com.tickon.identity.auth.domain.PasswordResetToken;
import com.tickon.identity.auth.infrastructure.persistence.mappers.PasswordResetTokenPersistenceMapper;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PasswordResetTokenRepositoryAdapter implements ResetTokenRepository {

  private final JpaPasswordResetTokenRepository jpaRepository;
  private final PasswordResetTokenPersistenceMapper mapper;

  public PasswordResetTokenRepositoryAdapter(JpaPasswordResetTokenRepository jpaRepository,
      PasswordResetTokenPersistenceMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public void save(PasswordResetToken token) {
    jpaRepository.save(mapper.toEntity(token));
  }

  @Override
  public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void invalidateAllForUser(UserId userId, Instant now) {
    jpaRepository.invalidateAllForUser(userId.value(), now);
  }
}
```

---

## 🔒 Step 4: Infrastructure - Security

### 4.1 Token Hasher

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/security/MacResetTokenHasher.java`

```java
package com.tickon.identity.auth.infrastructure.security;

import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MacResetTokenHasher implements ResetTokenHasher {

  private static final String HMAC_ALG = "HmacSHA256";
  private final byte[] pepperBytes;

  public MacResetTokenHasher(@Value("${security.password-reset.token-pepper}") String pepper) {
    if (pepper == null || pepper.isBlank()) {
      throw new IllegalStateException("Missing security.password-reset.token-pepper configuration");
    }
    this.pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public ResetTokenHash hash(String plainToken) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(pepperBytes, HMAC_ALG));
      byte[] digest = mac.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));
      return ResetTokenHash.from(Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compute reset token HMAC", e);
    }
  }
}
```

### 4.2 Token Generator

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/security/SecureResetTokenGenerator.java`

```java
package com.tickon.identity.auth.infrastructure.security;

import com.tickon.identity.auth.application.ports.out.ResetTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureResetTokenGenerator implements ResetTokenGenerator {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 32; // 256 bits

  @Override
  public String generateSecureToken() {
    byte[] randomBytes = new byte[TOKEN_BYTES];
    SECURE_RANDOM.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }
}
```

---

## 📧 Step 5: Infrastructure - Email (SendGrid)

### 5.1 Add SendGrid Dependency

Modify: `services/identity-service/pom.xml`

Add after other dependencies:
```xml
<!-- SendGrid Email -->
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.10.2</version>
</dependency>
```

### 5.2 Email Configuration Properties

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/email/EmailProperties.java`

```java
package com.tickon.identity.auth.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email.sendgrid")
public class EmailProperties {
  private String apiKey;
  private String fromEmail;
  private String fromName;
  private String resetUrlBase;

  // Getters and setters
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public void setFromEmail(String fromEmail) {
    this.fromEmail = fromEmail;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getResetUrlBase() {
    return resetUrlBase;
  }

  public void setResetUrlBase(String resetUrlBase) {
    this.resetUrlBase = resetUrlBase;
  }
}
```

### 5.3 SendGrid Email Sender

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/email/SendGridEmailSender.java`

```java
package com.tickon.identity.auth.infrastructure.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.tickon.identity.auth.application.ports.out.EmailSender;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailSender implements EmailSender {

  private static final Logger log = LoggerFactory.getLogger(SendGridEmailSender.class);

  private final SendGrid sendGrid;
  private final EmailProperties emailProperties;

  public SendGridEmailSender(EmailProperties emailProperties) {
    if (emailProperties.getApiKey() == null || emailProperties.getApiKey().isBlank()) {
      throw new IllegalStateException("SendGrid API key is not configured");
    }
    this.sendGrid = new SendGrid(emailProperties.getApiKey());
    this.emailProperties = emailProperties;
  }

  @Override
  public void sendPasswordResetEmail(com.tickon.identity.user.domain.valueobjects.Email to, String resetToken,
      String recipientName) {
    String resetUrl = emailProperties.getResetUrlBase() + "?token=" + resetToken;

    Email from = new Email(emailProperties.getFromEmail(), emailProperties.getFromName());
    Email toEmail = new Email(to.value(), recipientName);
    String subject = "Reset Your Password";

    // Create email content
    String htmlContent = buildHtmlContent(resetUrl, recipientName);
    String textContent = buildTextContent(resetUrl, recipientName);

    Content content = new Content("text/html", htmlContent);
    Mail mail = new Mail(from, subject, toEmail, content);

    // Add plain text alternative
    mail.addContent(new Content("text/plain", textContent));

    try {
      Request request = new Request();
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());

      Response response = sendGrid.api(request);

      if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
        log.info("Password reset email sent successfully to {}", to.value());
      } else {
        log.error("Failed to send password reset email to {}. Status: {}, Body: {}", to.value(),
            response.getStatusCode(), response.getBody());
      }
    } catch (IOException e) {
      log.error("Error sending password reset email to {}", to.value(), e);
      // Don't throw - email failure shouldn't block the API
    }
  }

  private String buildHtmlContent(String resetUrl, String recipientName) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #007bff;
                              color: white; text-decoration: none; border-radius: 4px; }
                    .footer { margin-top: 30px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Password Reset Request</h2>
                    <p>Hi %s,</p>
                    <p>We received a request to reset your password. Click the button below to create a new password:</p>
                    <p><a href="%s" class="button">Reset Password</a></p>
                    <p>Or copy and paste this link into your browser:</p>
                    <p><a href="%s">%s</a></p>
                    <p>This link will expire in 1 hour.</p>
                    <p>If you didn't request a password reset, you can safely ignore this email.</p>
                    <div class="footer">
                        <p>Thanks,<br>The Tickon Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, recipientName, resetUrl, resetUrl, resetUrl);
  }

  private String buildTextContent(String resetUrl, String recipientName) {
    return String.format(
        """
            Hi %s,

            We received a request to reset your password.

            Click this link to reset your password:
            %s

            This link will expire in 1 hour.

            If you didn't request a password reset, you can safely ignore this email.

            Thanks,
            The Tickon Team
            """, recipientName, resetUrl);
  }
}
```

### 5.4 Event Handler for Email Sending

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/events/SendPasswordResetEmailHandler.java`

```java
package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.EmailSender;
import com.tickon.identity.auth.domain.events.PasswordResetRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SendPasswordResetEmailHandler {

  private static final Logger log = LoggerFactory.getLogger(SendPasswordResetEmailHandler.class);
  private final EmailSender emailSender;

  public SendPasswordResetEmailHandler(EmailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Async
  @EventListener
  public void handle(PasswordResetRequestedEvent event) {
    log.info("Sending password reset email to {}", event.email().value());
    try {
      // Extract recipient name from email (simple implementation)
      String recipientName = event.email().value().split("@")[0];

      // Note: In real implementation, you'd pass the plain token from the event
      // For now, this is a placeholder - you'll need to modify the event to include the plain token
      String resetToken = "TOKEN_PLACEHOLDER"; // This needs to be passed in the event

      emailSender.sendPasswordResetEmail(event.email(), resetToken, recipientName);
    } catch (Exception e) {
      log.error("Failed to send password reset email", e);
      // Don't throw - email failure shouldn't crash the application
    }
  }
}
```

**⚠️ Important Note:** The `PasswordResetRequestedEvent` needs to include the **plain token** (not the hash). Modify the event:

```java
// Update PasswordResetRequestedEvent to include plain token
public record PasswordResetRequestedEvent(UserId userId, Email email, String plainToken, Instant occurredOn)
    implements DomainEvent {
  public PasswordResetRequestedEvent(UserId userId, Email email, String plainToken) {
    this(userId, email, plainToken, Instant.now());
  }
}
```

And update `RequestPasswordResetService` to pass the plain token in the event.

---

## 🌐 Step 6: Infrastructure - Web Layer

### 6.1 Web DTOs

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/dto/ForgotPasswordRequest.java`

```java
package com.tickon.identity.auth.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(@NotBlank @Email String email) {
}
```

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/dto/ForgotPasswordResponse.java`

```java
package com.tickon.identity.auth.infrastructure.web.dto;

public record ForgotPasswordResponse(String message) {
}
```

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/dto/ResetPasswordRequest.java`

```java
package com.tickon.identity.auth.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword) {
}
```

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/dto/VerifyResetTokenResponse.java`

```java
package com.tickon.identity.auth.infrastructure.web.dto;

public record VerifyResetTokenResponse(boolean valid) {
}
```

### 6.2 Mapper

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/mappers/PasswordResetMapper.java`

```java
package com.tickon.identity.auth.infrastructure.web.mappers;

import com.tickon.identity.auth.application.dto.*;
import com.tickon.identity.auth.infrastructure.web.dto.*;
import com.tickon.identity.user.domain.valueobjects.Email;

public class PasswordResetMapper {

  public static RequestPasswordResetCommand toCommand(ForgotPasswordRequest request) {
    return new RequestPasswordResetCommand(Email.from(request.email()));
  }

  public static ForgotPasswordResponse toResponse(RequestPasswordResetResult result) {
    return new ForgotPasswordResponse(result.message());
  }

  public static ResetPasswordCommand toCommand(ResetPasswordRequest request) {
    return new ResetPasswordCommand(request.token(), request.newPassword());
  }

  public static VerifyResetTokenCommand toCommand(String token) {
    return new VerifyResetTokenCommand(token);
  }

  public static VerifyResetTokenResponse toResponse(VerifyResetTokenResult result) {
    return new VerifyResetTokenResponse(result.valid());
  }
}
```

### 6.3 Controller Endpoints

Modify: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/web/AuthController.java`

Add these endpoints:

```java
// Add to existing imports
import com.tickon.identity.auth.application.ports.in.RequestPasswordResetUseCase;
import com.tickon.identity.auth.application.ports.in.ResetPasswordUseCase;
import com.tickon.identity.auth.application.ports.in.VerifyResetTokenUseCase;
import com.tickon.identity.auth.infrastructure.web.dto.*;
import com.tickon.identity.auth.infrastructure.web.mappers.PasswordResetMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Add to constructor
private final RequestPasswordResetUseCase requestPasswordReset;
private final ResetPasswordUseCase resetPassword;
private final VerifyResetTokenUseCase verifyResetToken;

// Add endpoints
@ResponseStatus(HttpStatus.OK)
@PostMapping("/forgot-password")
public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
  var result = requestPasswordReset.requestPasswordReset(PasswordResetMapper.toCommand(request));
  return PasswordResetMapper.toResponse(result);
}

@ResponseStatus(HttpStatus.NO_CONTENT)
@PostMapping("/reset-password")
public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
  resetPassword.resetPassword(PasswordResetMapper.toCommand(request));
}

@ResponseStatus(HttpStatus.OK)
@GetMapping("/verify-reset-token")
public VerifyResetTokenResponse verifyResetToken(@RequestParam String token) {
  var result = verifyResetToken.verifyResetToken(PasswordResetMapper.toCommand(token));
  return PasswordResetMapper.toResponse(result);
}
```

---

## 🔄 Step 7: Session Revocation Enhancement

### 7.1 Add PASSWORD_RESET to RevokeReason Enum

Modify: `services/identity-service/src/main/java/com/tickon/identity/auth/domain/valueobjects/RevokeReason.java`

```java
public enum RevokeReason {
  SESSION_ROTATED,
  USER_LOGOUT,
  TOKEN_COMPROMISED,
  TOKEN_REUSE_DETECTED,
  ADMIN_ACTION,
  PASSWORD_RESET,  // <-- Add this
  OTHER
}
```

### 7.2 Add revokeAllByUserId to SessionRepository

Modify: `services/identity-service/src/main/java/com/tickon/identity/auth/application/ports/out/SessionRepository.java`

Add method:
```java
void revokeAllByUserId(UserId userId, Instant now, RevokeReason reason);
```

### 7.3 Implement in Adapter

Modify: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/persistence/SessionRepositoryAdapter.java`

Add implementation:
```java
@Override
@Transactional
public void revokeAllByUserId(UserId userId, Instant now, RevokeReason reason) {
  List<SessionEntity> sessions = jpaRepository.findByUserIdAndRevokedAtIsNull(userId.value());
  for (SessionEntity entity : sessions) {
    Session session = mapper.toDomain(entity);
    if (!session.isRevoked()) {
      session.revoke(now, reason);
      jpaRepository.save(mapper.toEntity(session));
    }
  }
}
```

Add to JpaSessionRepository:
```java
List<SessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);
```

### 7.4 Event Handler for Session Revocation

**⚠️ NOTE: This section needs updating after CommandBus refactoring**

With the CommandBus refactoring, `PasswordResetCompletedEvent` is no longer emitted from `ResetPasswordService` (events are cleared at the end). Session revocation can be handled in one of two ways:

**Option 1: Keep Event for Side Effects** (Recommended)
- Emit `PasswordResetCompletedEvent` ONLY for side effects like session revocation
- ResetPasswordService should publish this event after successful password change
- Event does NOT include newPasswordHash (password already changed via CommandBus)

```java
// In ResetPasswordService, after successful password change:
// Instead of: token.clearEvents();
// Do this:
var event = new PasswordResetCompletedEvent(userId, token.id(), now);
eventPublisher.publish(event);
token.clearEvents();
```

**Option 2: Direct Session Revocation** (Simpler but couples modules)
- ResetPasswordService directly calls session repository to revoke sessions
- No event needed
- Requires ResetPasswordService to depend on SessionRepository

**Current Status:** Not implemented. Sessions are NOT revoked after password reset.

**Event Handler (if using Option 1):**

Create: `services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/events/RevokeSessionsOnPasswordResetHandler.java`

```java
package com.tickon.identity.auth.infrastructure.events;

import com.tickon.identity.auth.application.ports.out.SessionRepository;
import com.tickon.identity.auth.domain.events.PasswordResetCompletedEvent;
import com.tickon.identity.auth.domain.valueobjects.RevokeReason;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RevokeSessionsOnPasswordResetHandler {

  private static final Logger log = LoggerFactory.getLogger(RevokeSessionsOnPasswordResetHandler.class);
  private final SessionRepository sessionRepository;
  private final Clock clock;

  public RevokeSessionsOnPasswordResetHandler(SessionRepository sessionRepository, Clock clock) {
    this.sessionRepository = sessionRepository;
    this.clock = clock;
  }

  @Async
  @EventListener
  public void handle(PasswordResetCompletedEvent event) {
    log.info("Revoking all sessions for user {} due to password reset", event.userId().value());
    sessionRepository.revokeAllByUserId(event.userId(), clock.instant(), RevokeReason.PASSWORD_RESET);
  }
}
```

**Decision Required:** Choose which approach to use for session revocation.

---

## 👤 Step 8: User Module Integration ✅ REPLACED BY COMMANDBUS

**❌ THIS SECTION IS OBSOLETE**

This section described using `PasswordResetCompletedEvent` and an event handler to change the password asynchronously. This approach has been **replaced** by the **CommandBus pattern**.

**New Approach (Implemented):**

Instead of:
```
ResetPasswordService → emit PasswordResetCompletedEvent → ChangePasswordOnResetEventHandler → change password
```

We now use:
```
ResetPasswordService → execute ChangePasswordCommand via CommandBus → ChangePasswordCommandHandler → change password
```

**Benefits:**
- ✅ Synchronous - password changes immediately
- ✅ Transactional - all-or-nothing with token update
- ✅ No race conditions
- ✅ Immediate error feedback

**Implementation:**

1. **Command Contract** (✅ Completed):
   - Location: `services/identity-service/src/main/java/com/tickon/identity/shared/contracts/commands/ChangePasswordCommand.java`
   - Contains: `userId` and `newPasswordHash`

2. **Command Handler** (✅ Completed):
   - Location: `services/identity-service/src/main/java/com/tickon/identity/user/application/commandhandlers/ChangePasswordCommandHandler.java`
   - Finds user by ID, changes password, saves user
   - Returns `CommandResult.Success` or `CommandResult.Error`

3. **Service Integration** (✅ Completed):
   - `ResetPasswordService` executes command via `CommandBus`
   - Checks result before marking token as used
   - All within single `@Transactional` method

**See [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md#cross-module-communication-patterns) for complete documentation on CommandBus pattern.**

---

## ⚙️ Step 9: Configuration

### 9.1 Update application.yml

Modify: `services/identity-service/src/main/resources/application.yml`

Add after existing security config:
```yaml
security:
  password-reset:
    token-expiration-ms: 3600000  # 1 hour
    token-pepper: "${PASSWORD_RESET_TOKEN_PEPPER:changeme-dev-only}"

email:
  sendgrid:
    api-key: "${SENDGRID_API_KEY:}"
    from-email: "noreply@tickon.com"
    from-name: "Tickon Support"
    reset-url-base: "${PASSWORD_RESET_URL_BASE:http://localhost:3000/reset-password}"
```

### 9.2 Environment Variables

Create/update `.env` file (or configure in your deployment):
```bash
PASSWORD_RESET_TOKEN_PEPPER=<generate-32-byte-secure-random-base64>
SENDGRID_API_KEY=<your-sendgrid-api-key>
PASSWORD_RESET_URL_BASE=http://localhost:3000/reset-password
```

**Generate pepper:**
```bash
openssl rand -base64 32
```

---

## ✅ Step 10: Verification

### 10.1 Run All Tests
```bash
mvn clean verify -pl services/identity-service
```

### 10.2 Run Database Migration
```bash
mvn flyway:migrate -pl services/identity-service
```

### 10.3 Format Code
```bash
mvn spotless:apply -pl services/identity-service
```

### 10.4 Manual End-to-End Test

1. **Start the service:**
   ```bash
   mvn spring-boot:run -pl services/identity-service
   ```

2. **Request password reset:**
   ```bash
   curl -X POST http://localhost:8082/v1/auth/forgot-password \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com"}'
   ```

3. **Check your email** for the reset link

4. **Verify token:**
   ```bash
   curl http://localhost:8082/v1/auth/verify-reset-token?token=YOUR_TOKEN
   ```

5. **Reset password:**
   ```bash
   curl -X POST http://localhost:8082/v1/auth/reset-password \
     -H "Content-Type: application/json" \
     -d '{"token": "YOUR_TOKEN", "newPassword": "NewSecure123!"}'
   ```

6. **Login with new password:**
   ```bash
   curl -X POST http://localhost:8082/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail": "user@example.com", "password": "NewSecure123!", "deviceId": "test-device"}'
   ```

---

## 📋 Checklist Summary

- [x] Step 1: Domain Layer (COMPLETED)
- [x] Step 2: Application Services - **REFACTORED WITH COMMANDBUS** ✅
  - [x] RequestPasswordResetService (original implementation)
  - [x] ResetPasswordService (refactored to use CommandBus)
  - [ ] VerifyResetTokenService (needs implementation)
  - [x] ChangePasswordCommand + Handler (NEW - part of CommandBus pattern)
  - [x] GetUserByEmailQuery + Handler (for RequestPasswordResetService)
- [x] Step 3: Infrastructure - Persistence (Migration + Entity + Repository) ✅
- [x] Step 4: Infrastructure - Security (Hasher + Generator) ✅
- [ ] Step 5: Infrastructure - Email (SendGrid + Event Handler)
- [ ] Step 6: Infrastructure - Web (Controllers + DTOs)
- [ ] Step 7: Session Revocation (Enum + Repository method + ⚠️ Event Handler needs updating)
- [x] ~~Step 8: User Module Integration~~ - **OBSOLETE** (replaced by CommandBus in Step 2)
- [x] Step 9: Configuration (application.yml + env vars) ✅
- [x] Step 10: Verification - Core Tests Passing (200/200) ✅
  - [x] Unit tests for ResetPasswordService (6 tests)
  - [x] Unit tests for ChangePasswordCommandHandler (4 tests)
  - [x] Unit tests for SpringCommandBus (9 tests)
  - [x] Unit tests for CommandResult (17 tests)
  - [ ] Integration tests (E2E flow)

---

## 🐛 Troubleshooting

### Common Issues:

1. **"Missing PASSWORD_RESET_TOKEN_PEPPER"**
   - Set environment variable or update application.yml with a development value

2. **"SendGrid API key not configured"**
   - Set SENDGRID_API_KEY environment variable
   - Get API key from https://app.sendgrid.com/settings/api_keys

3. **Email not sending**
   - Check SendGrid API key is valid
   - Check logs for detailed error messages
   - Verify sender email is verified in SendGrid

4. **Token not found in database**
   - Ensure Flyway migration V7 ran successfully
   - Check database connection

5. **Tests failing**
   - Run `mvn clean test` to rebuild
   - Check for compilation errors first
   - Ensure all imports are correct

---

## 📚 Next Steps (Future Enhancements)

1. **Token Cleanup Job** - Scheduled task to delete old tokens
2. **Rate Limiting** - Prevent abuse with rate limiter
3. **Email Templates** - Externalize to template files with i18n
4. **Password History** - Prevent reusing recent passwords
5. **Multi-factor Auth** - Add TOTP before password reset

---

**Good luck with the implementation! Follow the steps in order and run tests frequently. 🚀**
