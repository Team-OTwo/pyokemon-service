package com.pyokemon.tenant.secret.jwt.dto;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JwtToken {
    private String token;
    private Integer expiresIn;
  }

  @Getter
  @RequiredArgsConstructor
  public static class AccessToken {
    private final JwtToken access;
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  public static class AccessRefreshToken {
    private final JwtToken access;
    private final JwtToken refresh;
  }
}
