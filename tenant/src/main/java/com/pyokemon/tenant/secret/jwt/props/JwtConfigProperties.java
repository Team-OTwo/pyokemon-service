package com.pyokemon.tenant.secret.jwt.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(value = "jwt", ignoreUnknownFields = true)
@Getter
@Setter
public class JwtConfigProperties {

  private Integer expiresIn;
  private Integer mobileExpiresIn;
  private Integer tabletExpiresIn;
  private String secretKey;

}
