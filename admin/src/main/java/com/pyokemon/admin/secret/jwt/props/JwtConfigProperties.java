package com.pyokemon.admin.secret.jwt.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(value = "jwt", ignoreUnknownFields = true)
@Getter
@Setter
public class JwtConfigProperties {

  private String secretKey;

  // 액세스 토큰 만료 시간(분)
  private Integer accessTokenValidityInMinutes;

  // 리프레시 토큰 만료 시간(일)
  private Integer refreshTokenValidityInDays;

  // 이전 버전과의 호환성을 위한 필드
  private Integer expiresIn;
}
