package com.pyokemon.account.auth.secret.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenGenerator tokenGenerator;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // 공개 API는 JWT 검증을 건너뛰고 다음 필터로 진행
    if (isPublicApi(requestURI)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = extractToken(request);

      if (token != null && tokenGenerator.validateToken(token)) {
        Claims claims = tokenGenerator.parseToken(token);

        // 사용자 정보를 request attribute에 설정 (기존 코드와 호환성)
        String accountId = claims.getSubject();
        String role = claims.get("role", String.class);

        request.setAttribute("X-Auth-AccountId", accountId);
        request.setAttribute("X-Auth-Role", role);

        // Spring Security 인증 객체 생성 - 권한 정보 포함
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(accountId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception e) {
      // 토큰 검증 실패 시 로그만 남기고 계속 진행
      log.warn("JWT token validation failed: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPublicApi(String requestURI) {
    return requestURI.equals("/accounts/api/login") || requestURI.equals("/accounts/api/tenants")
        || requestURI.equals("/accounts/api/users") || requestURI.equals("/accounts/api/health")
        || requestURI.startsWith("/accounts/actuator/");
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
