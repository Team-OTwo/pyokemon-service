package com.pyokemon.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pyokemon.account.auth.secret.jwt.JwtAuthenticationFilter;


@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            .anyRequest().permitAll());
//        .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//        .exceptionHandling(exceptionHandling -> exceptionHandling
//            .authenticationEntryPoint(customAuthenticationEntryPoint())
//            .accessDeniedHandler(customAccessDeniedHandler()));

    return http.build();
  }
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
