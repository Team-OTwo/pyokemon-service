package com.pyokemon.account.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pyokemon.account.auth.secret.jwt.JwtAuthenticationFilter;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final TokenGenerator tokenGenerator;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            // 공개 API
            .requestMatchers("/api/login", "/api/tenants", "/api/users").permitAll()
            .requestMatchers("/api/health", "/actuator/**").permitAll()
            // 관리자 전용 API
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // 테넌트 API (테넌트 본인 + 관리자)
            .requestMatchers("/api/tenants/profile/**").hasRole("TENANT")
            .requestMatchers("/api/tenants/**").hasAnyRole("TENANT", "ADMIN")
            // 사용자 API
            .requestMatchers("/api/users/profile/**").hasRole("USER")
            .requestMatchers("/api/users/**").hasRole("USER")
            // 기타 모든 요청은 인증 필요
            .anyRequest().authenticated())
        .addFilterBefore(new JwtAuthenticationFilter(tokenGenerator),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
