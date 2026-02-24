package com.tickon.identity.shared.platform.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class IdentityMdcFilterTest {

  private final IdentityMdcFilter filter = new IdentityMdcFilter();

  @AfterEach
  void cleanup() {
    MDC.clear();
  }

  @Test
  void shouldPopulateUserIdInMdcWhenHeaderPresent() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn("user-123");

    doAnswer(invocation -> {
      assertThat(MDC.get("userId")).isEqualTo("user-123");
      return null;
    }).when(chain).doFilter(request, response);

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldSkipUserIdWhenHeaderAbsent() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn(null);

    doAnswer(invocation -> {
      assertThat(MDC.get("userId")).isNull();
      return null;
    }).when(chain).doFilter(request, response);

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldSkipUserIdWhenHeaderIsEmptyString() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn("");

    doAnswer(invocation -> {
      assertThat(MDC.get("userId")).isNull();
      return null;
    }).when(chain).doFilter(request, response);

    filter.doFilterInternal(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldPreserveExistingMdcKeysAndRemoveOnlyUserId() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn("user-123");

    MDC.put("traceId", "t-1");

    doAnswer(invocation -> {
      assertThat(MDC.get("userId")).isEqualTo("user-123");
      assertThat(MDC.get("traceId")).isEqualTo("t-1");
      return null;
    }).when(chain).doFilter(request, response);

    filter.doFilterInternal(request, response, chain);

    assertThat(MDC.get("userId")).isNull();
    assertThat(MDC.get("traceId")).isEqualTo("t-1");
  }

  @Test
  void shouldClearMdcAfterRequestCompletes() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn("user-123");

    filter.doFilterInternal(request, response, chain);

    assertThat(MDC.get("userId")).isNull();
  }

  @Test
  void shouldClearMdcEvenWhenChainThrows() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(request.getHeader("X-User-Id")).thenReturn("user-123");
    doThrow(new RuntimeException("chain error")).when(chain).doFilter(request, response);

    try {
      filter.doFilterInternal(request, response, chain);
    } catch (RuntimeException ignored) {
    }

    assertThat(MDC.get("userId")).isNull();
  }
}
