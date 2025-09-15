package com.tickon.identityservice.user.config;

import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdService;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService;
import com.tickon.identityservice.user.application.ports.outbound.PasswordHasher;
import com.tickon.identityservice.user.application.ports.outbound.UserRepository;
import com.tickon.identityservice.user.application.usecases.GetUserById;
import com.tickon.identityservice.user.application.usecases.RegisterUser;
import com.tickon.identityservice.user.domain.policies.PasswordStrengthPolicy;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public RegisterUserService registerUserService(
      UserRepository userRepository,
      PasswordHasher passwordHasher,
      PasswordStrengthPolicy passwordPolicy,
      Clock clock) {
    return new RegisterUser(userRepository, passwordHasher, passwordPolicy, clock);
  }

  @Bean
  public GetUserByIdService getUserByIdService(UserRepository userRepository) {
    return new GetUserById(userRepository);
  }
}
