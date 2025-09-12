package com.tickon.identityservice.user.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdUseCase;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserUseCase;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.application.services.GetUserById;
import com.tickon.identityservice.user.application.services.RegisterUser;
import com.tickon.identityservice.user.domain.policies.PasswordStrengthPolicy;

@Configuration
public class UserConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public RegisterUserUseCase registerUserUseCase(UserRepository userRepository,
      PasswordHasher passwordHasher, PasswordStrengthPolicy passwordPolicy, Clock clock) {
    return new RegisterUser(userRepository, passwordHasher, passwordPolicy, clock);
  }

  @Bean
  public GetUserByIdUseCase getUserByIdUseCase(UserRepository userRepository) {
    return new GetUserById(userRepository);
  }
}
