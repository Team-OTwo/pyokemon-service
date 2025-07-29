package com.pyokemon.account.auth.secret.jwt;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.pyokemon.account.auth.secret.jwt.props.JwtConfigProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenGenerator {

  private final JwtConfigProperties jwtConfigProperties;

  public String generateAccessToken(Long accountId, String role) {
    return generateToken(accountId, role, jwtConfigProperties.getExpiresIn());
  }

  public String generateRefreshToken(Long accountId, String role) {
    return generateToken(accountId, role, jwtConfigProperties.getMobileExpiresIn());
  }

  private String generateToken(Long accountId, String role, long expirationTime) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expirationTime);

    return Jwts.builder().setSubject(String.valueOf(accountId)).claim("role", role).setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(Keys.hmacShaKeyFor(jwtConfigProperties.getSecretKey().getBytes()),
            SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(jwtConfigProperties.getSecretKey().getBytes())).build()
        .parseClaimsJws(token).getBody();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(jwtConfigProperties.getSecretKey().getBytes())).build()
          .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
