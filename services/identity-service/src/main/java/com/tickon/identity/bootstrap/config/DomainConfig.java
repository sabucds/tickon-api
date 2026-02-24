package com.tickon.identity.bootstrap.config;

import com.tickon.identity.user.domain.policies.PasswordStrengthPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

  @Bean
  public PasswordStrengthPolicy passwordStrengthPolicy() {
    return new PasswordStrengthPolicy();
  }
}
