package com.pyokemon.common.util.trace;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * Trace ID를 처리하는 간단한 필터 Gateway에서 전달된 X-Trace-Id 헤더를 MDC에 설정하거나 없으면 새로 생성 단순 로깅 목적으로만 사용
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends OncePerRequestFilter {

  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String TRACE_ID_MDC_KEY = "traceId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // 1. 헤더에서 trace ID 가져오기 (Gateway에서 전달)
    String traceId = request.getHeader(TRACE_ID_HEADER);

    // 2. 헤더에 없으면 새로 생성 (UUID 사용)
    if (traceId == null || traceId.isEmpty()) {
      traceId = generateTraceId();
    }

    // 3. MDC에 traceId 설정 (로그에서 사용)
    MDC.put(TRACE_ID_MDC_KEY, traceId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(TRACE_ID_MDC_KEY);
    }
  }

  private String generateTraceId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
