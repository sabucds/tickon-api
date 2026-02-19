package com.tickon.gateway.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class UserIdRelayFilterTest {

  private UserIdRelayFilter filter;

  @BeforeEach
  void setUp() {
    filter = new UserIdRelayFilter();
  }

  @Test
  void shouldAddUserIdHeader_WhenAuthenticatedRequest() {
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "ES256").subject("550e8400-e29b-41d4-a716-446655440000")
        .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build();
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

    MockServerHttpRequest request = MockServerHttpRequest.get("/v1/users/me").build();
    ServerWebExchange exchange = MockServerWebExchange.from(request).mutate().principal(Mono.just(authentication))
        .build();

    List<ServerWebExchange> captured = new ArrayList<>();
    filter.filter(exchange, ex -> {
      captured.add(ex);
      return Mono.empty();
    }).block();

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).getRequest().getHeaders().getFirst("X-User-Id"))
        .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
  }

  @Test
  void shouldStripIncomingUserIdHeader_WhenNotAuthenticated() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/v1/test").header("X-User-Id", "spoofed-id").build();
    ServerWebExchange exchange = MockServerWebExchange.from(request);

    List<ServerWebExchange> captured = new ArrayList<>();
    filter.filter(exchange, ex -> {
      captured.add(ex);
      return Mono.empty();
    }).block();

    assertThat(captured).hasSize(1);
    assertThat(captured.get(0).getRequest().getHeaders().getFirst("X-User-Id")).isNull();
  }

  @Test
  void shouldStripSpoofedUserIdHeader_WhenAuthenticated() {
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "ES256").subject("real-user-id").issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600)).build();
    JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

    MockServerHttpRequest request = MockServerHttpRequest.get("/v1/users/me").header("X-User-Id", "spoofed-id").build();
    ServerWebExchange exchange = MockServerWebExchange.from(request).mutate().principal(Mono.just(authentication))
        .build();

    List<ServerWebExchange> captured = new ArrayList<>();
    filter.filter(exchange, ex -> {
      captured.add(ex);
      return Mono.empty();
    }).block();

    assertThat(captured.get(0).getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("real-user-id");
  }
}
