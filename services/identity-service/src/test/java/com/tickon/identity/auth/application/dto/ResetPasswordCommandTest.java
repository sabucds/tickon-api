package com.tickon.identity.auth.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResetPasswordCommandTest {

  @Test
  void shouldRedactSensitiveFieldsInToString() {
    ResetPasswordCommand command = new ResetPasswordCommand("secret-token-123", "MyP@ssw0rd");

    String result = command.toString();

    assertThat(result).doesNotContain("secret-token-123");
    assertThat(result).doesNotContain("MyP@ssw0rd");
    assertThat(result).contains("ResetPasswordCommand");
    assertThat(result).contains("REDACTED");
  }
}
