package com.pyokemon.user.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig {

  /**
   * 비밀번호 암호화를 위한 PasswordEncoder Bean
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Spring Security 기본 설정 (Gateway에서 인증 처리하므로 최소한만)
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth.requestMatchers("/api/users/login").permitAll()
            .requestMatchers("/api/users/register").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll() // Swagger 허용
            .anyRequest().permitAll() // Gateway에서 인증하므로 모든 요청 허용
        );

    return http.build();
  }
}
