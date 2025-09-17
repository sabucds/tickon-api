package com.tickon.identityservice.user.application.usecases;

import com.tickon.identityservice.user.application.models.UserResponseModel;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService.RegisterUserCommand;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.domain.User;
import com.tickon.identityservice.user.domain.policies.PasswordStrengthPolicy;
import com.tickon.identityservice.user.domain.valueobjects.Email;
import com.tickon.identityservice.user.domain.valueobjects.PasswordHash;
import com.tickon.identityservice.user.domain.valueobjects.Username;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordHasher passwordHasher;
    
    @Mock
    private PasswordStrengthPolicy passwordPolicy;
    
    private Clock fixedClock;
    private RegisterUser registerUser;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
        fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        
        registerUser = new RegisterUser(userRepository, passwordHasher, passwordPolicy, fixedClock);
    }

    @Test
    void shouldRegisterUser_WhenValidInput() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        
        var hashedPassword = new PasswordHash("hashed-password");
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        when(passwordHasher.hash("SecurePass123!")).thenReturn(hashedPassword);

        UserResponseModel result = registerUser.register(command);
        
        assertThat(result.username()).isEqualTo("johndoe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.id()).isNotNull();
        
        verify(passwordPolicy).validate("SecurePass123!");
        verify(passwordHasher).hash("SecurePass123!");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.email().value()).isEqualTo("john@example.com");
        assertThat(savedUser.username().value()).isEqualTo("johndoe");
        assertThat(savedUser.firstName()).isEqualTo("John");
        assertThat(savedUser.lastName()).isEqualTo("Doe");
        assertThat(savedUser.passwordHash()).isEqualTo(hashedPassword);
        assertThat(savedUser.createdAt()).isEqualTo(fixedInstant);
    }

    @Test
    void shouldThrowException_WhenEmailAlreadyExists() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
        
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Email already in use");
    }

    @Test
    void shouldThrowException_WhenUsernameAlreadyExists() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);
        
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Username already in use");
    }

    @Test
    void shouldThrowException_WhenPasswordPolicyValidationFails() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "weak"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        doThrow(new IllegalArgumentException("Password too weak"))
            .when(passwordPolicy).validate("weak");
        
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Password too weak");
    }

    @Test
    void shouldThrowException_WhenInvalidEmailFormat() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "invalid-email", "SecurePass123!"
        );
        
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid email");
    }

    @Test
    void shouldThrowException_WhenInvalidUsernameFormat() {
        var command = new RegisterUserCommand(
            "John", "Doe", "", "john@example.com", "SecurePass123!"
        );
        
        assertThatThrownBy(() -> registerUser.register(command))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUseClockForCreatedAt() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));
        
        registerUser.register(command);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.createdAt()).isEqualTo(fixedInstant);
    }

    @Test
    void shouldCreateUniqueUserIds_WhenRegisteringMultipleUsers() {
        var command1 = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        var command2 = new RegisterUserCommand(
            "Jane", "Smith", "janesmith", "jane@example.com", "SecurePass456!"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));
        
        UserResponseModel result1 = registerUser.register(command1);
        UserResponseModel result2 = registerUser.register(command2);
        
        assertThat(result1.id()).isNotEqualTo(result2.id());
    }

    @Test
    void shouldValidatePasswordBeforeHashing() {
        var command = new RegisterUserCommand(
            "John", "Doe", "johndoe", "john@example.com", "SecurePass123!"
        );
        
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
        when(passwordHasher.hash(any())).thenReturn(new PasswordHash("hashed"));
        
        registerUser.register(command);

        var inOrder = org.mockito.Mockito.inOrder(passwordPolicy, passwordHasher);
        inOrder.verify(passwordPolicy).validate("SecurePass123!");
        inOrder.verify(passwordHasher).hash("SecurePass123!");
    }
}
