//package com.pyokemon.account.auth.secret.jwt;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//      FilterChain filterChain) throws ServletException, IOException {
//
//    String requestURI = request.getRequestURI();
//
//    // 공개 API
//    if (isPublicApi(requestURI)) {
//      filterChain.doFilter(request, response);
//      return;
//    }
//
//    try {
//      // Gateway에서 전달받은 헤더 정보 검증
//      String accountId = extractAccountId(request);
//      String role = extractRole(request);
//
//      if (accountId == null || role == null) {
//        throw new BadCredentialsException("인증 정보가 없습니다.");
//      }
//
//      request.setAttribute("X-Auth-AccountId", accountId);
//      request.setAttribute("X-Auth-Role", role);
//
//      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//
//      UsernamePasswordAuthenticationToken authentication =
//          new UsernamePasswordAuthenticationToken(accountId, null, authorities);
//
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//
//    } catch (AuthenticationException e) {
//      // Spring Security 예외는 그대로 던져서 AuthenticationEntryPoint에서 처리
//      throw e;
//    } catch (Exception e) {
//      // 기타 예외는 BadCredentialsException으로 래핑
//      log.error("Authentication validation failed: {}", e.getMessage(), e);
//      throw new BadCredentialsException("인증 검증 중 오류가 발생했습니다.", e);
//    }
//
//    filterChain.doFilter(request, response);
//  }
//
//  private boolean isPublicApi(String requestURI) {
//    String actualPath = requestURI.replace("/account", "");
//    return actualPath.equals("/api/login") || actualPath.equals("/api/login/app") || actualPath.equals("/api/users") || actualPath.equals("/api/tenants");
//  }
//
//  private String extractAccountId(HttpServletRequest request) {
//    // Gateway에서 전달받은 헤더에서 accountId 추출
//    String accountId = request.getHeader("X-Auth-AccountId");
//    if (accountId == null || accountId.trim().isEmpty()) {
//      return null;
//    }
//    return accountId.trim();
//  }
//
//  private String extractRole(HttpServletRequest request) {
//    // Gateway에서 전달받은 헤더에서 role 추출
//    String role = request.getHeader("X-Auth-Role");
//    if (role == null || role.trim().isEmpty()) {
//      return null;
//    }
//    return role.trim();
//  }
//}
