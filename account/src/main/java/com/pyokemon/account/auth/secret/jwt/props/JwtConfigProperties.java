package com.pyokemon.account.auth.secret.jwt.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfigProperties {

  private String secretKey;
  private long expiresIn;
  private long mobileExpiresIn;
}
