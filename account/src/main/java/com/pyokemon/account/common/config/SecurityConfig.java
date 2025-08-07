package com.pyokemon.account.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pyokemon.account.auth.secret.jwt.JwtAuthenticationFilter;
import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

  private final ObjectMapper objectMapper = new ObjectMapper();

//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http.csrf(csrf -> csrf.disable())
//        .sessionManagement(
//            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//        .authorizeHttpRequests(authz -> authz
//            // 공개 API
//            .requestMatchers("/api/login", "/api/users", "/api/tenants", "/api/login/app").permitAll()
//            // 관리자 전용 API
//            .requestMatchers("/api/admin/**").hasRole("ADMIN")
//            // 테넌트 API (테넌트 본인 + 관리자)
//            .requestMatchers("/api/tenants/profile/**").hasRole("TENANT")
//            .requestMatchers("/api/tenants/**").hasAnyRole("TENANT", "ADMIN")
//            // 사용자 API
//            .requestMatchers("/api/users/profile/**").hasRole("USER")
//            .requestMatchers("/api/users/**").hasRole("USER")
//            // 기타 모든 요청은 인증 필요
//            .anyRequest().authenticated())
//        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//        .exceptionHandling(exceptionHandling -> exceptionHandling
//            .authenticationEntryPoint(customAuthenticationEntryPoint())
//            .accessDeniedHandler(customAccessDeniedHandler()));
//
//    return http.build();
//  }
//
//  @Bean
//  public AuthenticationEntryPoint customAuthenticationEntryPoint() {
//    return (request, response, authException) -> {
//      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//      response.setCharacterEncoding("UTF-8");
//
//      ResponseDto<Void> errorResponse =
//          ResponseDto.error("인증이 필요합니다.", AccountErrorCodes.ACCESS_DENIED);
//      String jsonResponse = objectMapper.writeValueAsString(errorResponse);
//      response.getWriter().write(jsonResponse);
//    };
//  }
//
//  @Bean
//  public AccessDeniedHandler customAccessDeniedHandler() {
//    return (request, response, accessDeniedException) -> {
//      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//      response.setCharacterEncoding("UTF-8");
//
//      ResponseDto<Void> errorResponse =
//          ResponseDto.error("접근 권한이 없습니다.", AccountErrorCodes.PERMISSION_DENIED);
//      String jsonResponse = objectMapper.writeValueAsString(errorResponse);
//      response.getWriter().write(jsonResponse);
//    };
//  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
