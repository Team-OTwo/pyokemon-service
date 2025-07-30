package com.pyokemon.account.auth.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

class AuthConstants {
    public static final String BLACKLIST_PREFIX = "blacklist:";
    public static final String BEARER_PREFIX = "Bearer ";
}

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public LoginResponseDto login(LoginRequestDto request) {
    log.info("로그인 시도: {}", request.getLoginId());
    // 1. 로그인 ID로 계정 조회
    Account account = accountRepository.findByLoginId(request.getLoginId())
        .orElseThrow(() -> new BusinessException(AccountErrorCodes.ACCOUNT_NOT_FOUND, 
                                               "계정을 찾을 수 없습니다."));
    
    // 2. 계정 상태 확인 (삭제된 계정인지)
    if ("DELETED".equals(account.getStatus())) {
        throw new BusinessException(AccountErrorCodes.ACCOUNT_DELETED, 
                                  "삭제된 계정입니다.");
    }
    
    // 3. 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
        throw new BusinessException(AccountErrorCodes.PASSWORD_MISMATCH, 
                                  "비밀번호가 일치하지 않습니다.");
    }
    
    // 4. JWT 토큰 생성
    String accessToken = tokenGenerator.generateAccessToken(account.getAccountId(), account.getRole());
    String refreshToken = tokenGenerator.generateRefreshToken(account.getAccountId(), account.getRole());
    
    // 5. 응답 데이터 구성
    return LoginResponseDto.builder()
        .accountId(account.getAccountId())
        .role(account.getRole())
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Transactional
  public void logout(String token) {
    // 토큰이 null인 경우 처리
    if (token == null) {
        return; 
    }
    
    // 토큰에서 "Bearer " 접두사 제거
    if (token != null && token.startsWith(AuthConstants.BEARER_PREFIX)) {
        token = token.substring(AuthConstants.BEARER_PREFIX.length());
    }
    
    // Redis에 토큰 블랙리스트 추가
    // 토큰의 남은 유효 시간 계산
    try {
        Claims claims = tokenGenerator.parseToken(token);
        Date expiration = claims.getExpiration();
        long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        
        if (ttl > 0) {
            // Redis에 블랙리스트 추가 (토큰을 키로, 값은 "blacklisted"로 설정)
            redisTemplate.opsForValue().set(AuthConstants.BLACKLIST_PREFIX + token, "blacklisted", ttl, TimeUnit.SECONDS);
        }
    } catch (Exception e) {
        // 토큰 파싱 실패 시 무시 (이미 만료된 토큰일 수 있음)
    }
  }

  @Transactional
  public TokenResponseDto refreshToken(String refreshToken) {
    // 1. 리프레시 토큰 유효성 검증
    if (!tokenGenerator.validateToken(refreshToken)) {
        throw new BusinessException(AccountErrorCodes.TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
    }
    
    // 2. 블랙리스트 확인
    if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken))) {
        throw new BusinessException(AccountErrorCodes.TOKEN_INVALID, "로그아웃된 토큰입니다.");
    }
    
    // 3. 토큰에서 사용자 정보 추출
    Claims claims = tokenGenerator.parseToken(refreshToken);
    String accountIdStr = claims.getSubject();
    String role = claims.get("role", String.class);
    
    if (accountIdStr == null || role == null) {
        throw new BusinessException(AccountErrorCodes.TOKEN_INVALID, "토큰에서 사용자 정보를 추출할 수 없습니다.");
    }
    
    Long accountId = Long.parseLong(accountIdStr);
    
    // 4. 계정 존재 확인
    Account account = accountRepository.findByAccountId(accountId)
        .orElseThrow(() -> new BusinessException(AccountErrorCodes.ACCOUNT_NOT_FOUND, "계정을 찾을 수 없습니다."));
    
    // 5. 계정 상태 확인
    if ("DELETED".equals(account.getStatus())) {
        throw new BusinessException(AccountErrorCodes.ACCOUNT_DELETED, "삭제된 계정입니다.");
    }
    
    // 6. 새 액세스 토큰 생성
    String newAccessToken = tokenGenerator.generateAccessToken(accountId, role);
    
    // 7. 응답 데이터 구성
    return TokenResponseDto.builder()
        .accessToken(newAccessToken)
        .build();
  }

  @Transactional
  public void changePassword(Long accountId, UpdatePasswordRequestDto request) {
    // 1. 계정 조회
    Account account = accountRepository.findByAccountId(accountId)
        .orElseThrow(() -> new BusinessException(AccountErrorCodes.ACCOUNT_NOT_FOUND, "계정을 찾을 수 없습니다."));
    
    // 2. 현재 비밀번호 확인
    if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
        throw new BusinessException(AccountErrorCodes.PASSWORD_MISMATCH, "현재 비밀번호가 일치하지 않습니다.");
    }
    
    // 3. 새 비밀번호가 현재 비밀번호와 같은지 확인
    if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
        throw new BusinessException(AccountErrorCodes.INVALID_PASSWORD, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }
    
    // 4. 새 비밀번호 암호화 및 저장
    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
    account.setPassword(encodedNewPassword);
    
    // 5. 저장
    accountRepository.update(account);
  }

  @Transactional
  public void deleteAccount(Long accountId) {
    // 계정 상태를 DELETED로 변경
    accountRepository.updateStatus(accountId, "DELETED");
  }
}
