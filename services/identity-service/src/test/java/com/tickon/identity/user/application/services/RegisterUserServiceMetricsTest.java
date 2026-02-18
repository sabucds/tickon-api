package com.tickon.identity.user.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.identity.shared.infrastructure.metrics.IdentityMetrics;
import com.tickon.identity.shared.ports.out.DomainEventPublisher;
import com.tickon.identity.user.application.dto.RegisterUserCommand;
import com.tickon.identity.user.application.ports.out.PasswordHasher;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.exceptions.DuplicateEmailException;
import com.tickon.identity.user.domain.exceptions.DuplicateUsernameException;
import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identity.user.domain.valueobjects.Username;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterUserServiceMetricsTest {

  private UserRepository userRepository;
  private PasswordHasher passwordHasher;
  private PasswordStrengthPolicy passwordPolicy;
  private DomainEventPublisher eventPublisher;
  private SimpleMeterRegistry registry;
  private IdentityMetrics metrics;
  private RegisterUserService registerUserService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    passwordHasher = mock(PasswordHasher.class);
    passwordPolicy = new PasswordStrengthPolicy();
    eventPublisher = mock(DomainEventPublisher.class);
    registry = new SimpleMeterRegistry();
    metrics = new IdentityMetrics(registry);

    registerUserService = new RegisterUserService(userRepository, passwordHasher, passwordPolicy, eventPublisher,
        metrics);
  }

  @Test
	void successfulRegistrationIncrementsUserRegisteredCounter() {
		when(userRepository.existsByEmail(any())).thenReturn(false);
		when(userRepository.existsByUsername(any())).thenReturn(false);
		when(passwordHasher.hash(any())).thenReturn(new com.tickon.common.identity.domain.valueobjects.PasswordHash("hashed"));

		registerUserService.register(new RegisterUserCommand("FirstName", "LastName", new Username("username"),
				new Email("user@example.com"), "Password1!"));

		assertThat(registry.find("identity.user.registered").counter()).isNotNull();
		assertThat(registry.find("identity.user.registered").counter().count()).isEqualTo(1.0);
	}

  @Test
	void duplicateEmailIncrementsFailureWithDuplicateEmailReason() {
		when(userRepository.existsByEmail(any())).thenReturn(true);

		assertThatThrownBy(() -> registerUserService.register(new RegisterUserCommand("FirstName", "LastName",
				new Username("username"), new Email("user@example.com"), "Password1!")))
				.isInstanceOf(DuplicateEmailException.class);

		assertThat(registry.find("identity.user.registration.failed").tag("reason", "duplicate_email").counter())
				.isNotNull();
		assertThat(registry.find("identity.user.registration.failed").tag("reason", "duplicate_email").counter()
				.count()).isEqualTo(1.0);
	}

  @Test
	void duplicateUsernameIncrementsFailureWithDuplicateUsernameReason() {
		when(userRepository.existsByEmail(any())).thenReturn(false);
		when(userRepository.existsByUsername(any())).thenReturn(true);

		assertThatThrownBy(() -> registerUserService.register(new RegisterUserCommand("FirstName", "LastName",
				new Username("username"), new Email("user@example.com"), "Password1!")))
				.isInstanceOf(DuplicateUsernameException.class);

		assertThat(registry.find("identity.user.registration.failed").tag("reason", "duplicate_username").counter())
				.isNotNull();
		assertThat(registry.find("identity.user.registration.failed").tag("reason", "duplicate_username").counter()
				.count()).isEqualTo(1.0);
	}
}
