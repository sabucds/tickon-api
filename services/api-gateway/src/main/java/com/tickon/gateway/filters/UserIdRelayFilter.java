package com.tickon.gateway.filters;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserIdRelayFilter implements GlobalFilter, Ordered {

  static final String USER_ID_HEADER = "X-User-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return exchange.getPrincipal().filter(p -> p instanceof JwtAuthenticationToken).cast(JwtAuthenticationToken.class)
        .map(jwt -> withUserIdHeader(exchange, jwt.getToken().getSubject()))
        .switchIfEmpty(Mono.just(stripUserIdHeader(exchange))).flatMap(chain::filter);
  }

  private ServerWebExchange stripUserIdHeader(ServerWebExchange exchange) {
    return withUserIdHeader(exchange, null);
  }

  private ServerWebExchange withUserIdHeader(ServerWebExchange exchange, String userIdOrNull) {
    ServerHttpRequest original = exchange.getRequest();

    HttpHeaders newHeaders = new HttpHeaders();
    newHeaders.putAll(original.getHeaders());
    newHeaders.remove(USER_ID_HEADER);

    if (userIdOrNull != null && !userIdOrNull.isBlank()) {
      newHeaders.put(USER_ID_HEADER, List.of(userIdOrNull)); // single value, no duplicates
    }

    ServerHttpRequest decorated = new ServerHttpRequestDecorator(original) {
      @Override
      public HttpHeaders getHeaders() {
        return newHeaders;
      }
    };

    return exchange.mutate().request(decorated).build();
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
