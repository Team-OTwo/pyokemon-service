package com.pyokemon.admin.secret.jwt;

import com.pyokemon.admin.secret.jwt.dto.TokenDto;
import com.pyokemon.admin.secret.jwt.props.JwtConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenGenerator {

  private final JwtConfigProperties configProperties;

  // JWT 서명을 위한 비밀 키
  private volatile SecretKey secretKey;

  // 비밀키를 처음 한번만 생성하고 재사용
  private SecretKey getSecretKey() {
    if (secretKey == null) {
      synchronized (this) {
        if (secretKey == null) {
          // 문자열 그대로 사용하여 키 생성 (Base64 디코딩 없이)
          secretKey = Keys.hmacShaKeyFor(configProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        }
      }
    }
    return secretKey;
  }

  // 기기 타입, 토큰 종류에 따른 만료 시간을 정하는 로직
  private int tokenExpiresIn(boolean refreshToken) {
    int expiresIn;
    if (refreshToken) {
      // 리프레시 토큰은 일 단위로 설정되어 있으므로 초 단위로 변환
      // 설정값이 없으면 기본값 7일 사용
      int days = configProperties.getRefreshTokenValidityInDays() != null ? 
          configProperties.getRefreshTokenValidityInDays() : 7;
      expiresIn = days * 24 * 60 * 60;
    } else {
      // 액세스 토큰은 분 단위로 설정되어 있으므로 초 단위로 변환
      // 설정값이 없으면 기본값 30분 사용
      int minutes = configProperties.getAccessTokenValidityInMinutes() != null ? 
          configProperties.getAccessTokenValidityInMinutes() : 30;
      expiresIn = minutes * 60;
    }
    return expiresIn;
  }

  // JWT 토큰을 실제로 생성하는 메서드
  public TokenDto.JwtToken generateJwtToken(String userId,
      boolean refreshToken) {
    int tokenExpiresIn = tokenExpiresIn(refreshToken);
    String tokenType = refreshToken ? "refresh" : "access";
    
    log.info("토큰 생성 시작 - 타입: {}, 사용자: {}, 만료시간: {}초", tokenType, userId, tokenExpiresIn);

    // JWT Token 생성
    String token = Jwts.builder()
        .setSubject(userId)
        .claim("userId", userId)  // 명시적으로 userId 클레임 설정
        .claim("tokenType", tokenType)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + tokenExpiresIn * 1000L))
        .setHeaderParam("typ", "JWT")
        .signWith(getSecretKey())
        .compact();
    
    log.info("토큰 생성 완료 - 타입: {}, 토큰: {}", tokenType, token);

    return new TokenDto.JwtToken(token, tokenExpiresIn);
  }

  // 엑세스 토큰만 발급하는 메서드
  public TokenDto.AccessToken generateAccessToken(String userId) {
    TokenDto.JwtToken jwtToken = this.generateJwtToken(userId, false);
    return new TokenDto.AccessToken(jwtToken);
  }

  // 엑세스, 리프레시 토큰 발급하는 메서드
  public TokenDto.AccessRefreshToken generateAccessRefreshToken(String userId, String deviceType) {
    TokenDto.JwtToken accessJwtToken = this.generateJwtToken(userId, false);
    TokenDto.JwtToken refreshJwtToken = this.generateJwtToken(userId, true);
    return new TokenDto.AccessRefreshToken(accessJwtToken, refreshJwtToken);
  }

  // refresh token 유효성 검증 메서드
  public String validateJwtToken(String refreshToken) {
    String userId = null;

    final Claims claims = this.verifyAndGetClaims(refreshToken);

    if (claims == null) {
      log.warn("리프레시 토큰 검증 실패: claims가 null입니다.");
      return null;
    }

    Date expirationDate = claims.getExpiration();
    if (expirationDate == null || expirationDate.before(new Date())) {
      log.warn("리프레시 토큰 검증 실패: 토큰이 만료되었습니다. 만료일: {}", expirationDate);
      return null;
    }

    // 먼저 subject에서 userId 확인
    userId = claims.getSubject();
    if (userId == null) {
      // subject에 없으면 userId 클레임에서 확인
      userId = claims.get("userId", String.class);
      if (userId == null) {
        log.warn("리프레시 토큰 검증 실패: userId가 없습니다.");
        // 디버깅을 위해 모든 클레임 출력
        log.warn("토큰 클레임: {}", claims.toString());
        return null;
      }
    }

    String tokenType = claims.get("tokenType", String.class);
    if (!"refresh".equals(tokenType)) {
      log.warn("리프레시 토큰 검증 실패: 토큰 타입이 refresh가 아닙니다. 타입: {}", tokenType);
      return null;
    }

    log.info("리프레시 토큰 검증 성공: 사용자 ID = {}", userId);
    return userId;
  }

  // access token에서 사용자 ID 추출 메서드
  public String validateAccessToken(String accessToken) {
    final Claims claims = this.verifyAndGetClaims(accessToken);

    if (claims == null) {
      log.warn("액세스 토큰 검증 실패: claims가 null입니다.");
      return null;
    }

    Date expirationDate = claims.getExpiration();
    if (expirationDate == null || expirationDate.before(new Date())) {
      log.warn("액세스 토큰 검증 실패: 토큰이 만료되었습니다. 만료일: {}", expirationDate);
      return null;
    }

    String tokenType = claims.get("tokenType", String.class);
    if (!"access".equals(tokenType)) {
      log.warn("액세스 토큰 검증 실패: 토큰 타입이 access가 아닙니다. 타입: {}", tokenType);
      return null;
    }

    // 먼저 subject에서 userId 확인
    String userId = claims.getSubject();
    if (userId == null) {
      // subject에 없으면 userId 클레임에서 확인
      userId = claims.get("userId", String.class);
      if (userId == null) {
        log.warn("액세스 토큰 검증 실패: userId가 없습니다.");
        return null;
      }
    }
    
    log.info("액세스 토큰 검증 성공: 사용자 ID = {}", userId);
    return userId;
  }

  // 토큰에서 claims를 꺼내는 내부 메서드
  private Claims verifyAndGetClaims(String token) {
    Claims claims;

    try {
      log.info("토큰 검증 시도: {}", token);
      claims = Jwts.parserBuilder()
          .setSigningKey(getSecretKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
      log.info("토큰 검증 성공: {}", claims);
    } catch (Exception e) {
      log.error("토큰 검증 실패: {} - {}", e.getClass().getName(), e.getMessage());
      claims = null;
    }
    return claims;
  }
}
