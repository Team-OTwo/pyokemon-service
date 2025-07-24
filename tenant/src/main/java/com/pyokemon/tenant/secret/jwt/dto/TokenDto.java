package com.pyokemon.tenant.secret.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenDto {

  public record JwtToken(String token, Integer expiresIn) {}

  public record AccessToken(JwtToken access) {}

  public record AccessRefreshToken(JwtToken access, JwtToken refresh) {}

  @Data
  @NoArgsConstructor
  public static class RefreshRequest {
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
  }
}
