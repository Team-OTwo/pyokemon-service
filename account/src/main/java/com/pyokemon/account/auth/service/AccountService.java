package com.pyokemon.account.auth.service;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.constants.AuthConstants;
import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.auth.secret.jwt.TokenGenerator;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    // 계정 조회
    Optional<Account> accountOpt =
        accountRepository.findByLoginIdAndStatus(request.getLoginId(), AccountStatus.ACTIVE);
    if (accountOpt.isEmpty()) {
      log.warn("로그인 실패: 계정을 찾을 수 없음 - {}", request.getLoginId());
      throw new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND);
    }

    Account account = accountOpt.get();

    // 비밀번호 확인
    if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
      log.warn("로그인 실패: 비밀번호 불일치 - {}", request.getLoginId());
      throw new BusinessException("로그인 ID 또는 비밀번호가 올바르지 않습니다.", AccountErrorCodes.INVALID_LOGIN);
    }

    // JWT 토큰 생성
    String accessToken =
        tokenGenerator.generateAccessToken(account.getAccountId(), account.getRole());
    String refreshToken =
        tokenGenerator.generateRefreshToken(account.getAccountId(), account.getRole());

    log.info("로그인 성공: {} (역할: {})", request.getLoginId(), account.getRole());

    return LoginResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken)
        .role(account.getRole()).accountId(account.getAccountId()).build();
  }

  @Transactional
  public TokenResponseDto refreshToken(String refreshToken) {
    log.info("토큰 갱신 시도");

    // 1. 리프레시 토큰 유효성 검증
    if (!tokenGenerator.validateToken(refreshToken)) {
      log.warn("토큰 갱신 실패: 유효하지 않은 토큰");
      throw new BusinessException("유효하지 않은 리프레시 토큰입니다.", AccountErrorCodes.INVALID_TOKEN);
    }

    // 2. 블랙리스트 확인
    if (Boolean.TRUE.equals(redisTemplate.hasKey(AuthConstants.BLACKLIST_PREFIX + refreshToken))) {
      log.warn("토큰 갱신 실패: 블랙리스트에 등록된 토큰");
      throw new BusinessException("로그아웃된 토큰입니다.", AccountErrorCodes.INVALID_TOKEN);
    }

    try {
      // 3. 토큰에서 사용자 정보 추출
      Claims claims = tokenGenerator.parseToken(refreshToken);
      String accountIdStr = claims.getSubject();
      String role = claims.get("role", String.class);

      if (accountIdStr == null || role == null) {
        log.warn("토큰 갱신 실패: 토큰에서 사용자 정보를 추출할 수 없음");
        throw new BusinessException("토큰에서 사용자 정보를 추출할 수 없습니다.", AccountErrorCodes.INVALID_TOKEN);
      }

      Long accountId = Long.parseLong(accountIdStr);

      // 4. 계정 존재 확인
      Account account = accountRepository.findByAccountId(accountId).orElseThrow(() -> {
        log.warn("토큰 갱신 실패: 계정을 찾을 수 없음 - ID: {}", accountId);
        return new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND);
      });

      // 5. 계정 상태 확인
      if (account.getStatus() == AccountStatus.DELETED) {
        log.warn("토큰 갱신 실패: 삭제된 계정 - ID: {}", accountId);
        throw new BusinessException("삭제된 계정입니다.", AccountErrorCodes.ACCOUNT_DELETED);
      }

      // 6. 새 액세스 토큰 생성
      String newAccessToken = tokenGenerator.generateAccessToken(accountId, role);

      log.info("토큰 갱신 성공: 계정 ID: {}, 역할: {}", accountId, role);

      // 7. 응답 데이터 구성
      return TokenResponseDto.builder().accessToken(newAccessToken).build();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("토큰 갱신 중 예외 발생: {}", e.getMessage());
      throw new BusinessException("유효하지 않은 토큰입니다.", AccountErrorCodes.INVALID_TOKEN);
    }
  }

  @Transactional
  public void changePassword(Long accountId, UpdatePasswordRequestDto request) {
    log.info("비밀번호 변경 시도: 계정 ID: {}", accountId);

    // 1. 계정 조회
    Account account = accountRepository.findByAccountId(accountId).orElseThrow(() -> {
      log.warn("비밀번호 변경 실패: 계정을 찾을 수 없음 - ID: {}", accountId);
      return new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND);
    });

    // 2. 현재 비밀번호 확인
    if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
      log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 - ID: {}", accountId);
      throw new BusinessException("현재 비밀번호가 일치하지 않습니다.", AccountErrorCodes.PASSWORD_MISMATCH);
    }

    // 3. 새 비밀번호가 현재 비밀번호와 같은지 확인
    if (passwordEncoder.matches(request.getNewPassword(), account.getPassword())) {
      log.warn("비밀번호 변경 실패: 새 비밀번호가 현재 비밀번호와 동일함 - ID: {}", accountId);
      throw new BusinessException("새 비밀번호는 현재 비밀번호와 달라야 합니다.", AccountErrorCodes.PASSWORD_MATCH);
    }

    // 4. 새 비밀번호 암호화 및 저장
    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
    account.setPassword(encodedNewPassword);

    // 5. 저장
    accountRepository.update(account);
    log.info("비밀번호 변경 성공: 계정 ID: {}", accountId);
  }

  @Transactional
  public void logout(String token) {
    log.info("로그아웃 시도");

    // 토큰이 null인 경우 처리
    if (token == null) {
      log.warn("로그아웃 실패: 토큰이 없음");
      return;
    }

    // 토큰에서 "Bearer " 접두사 제거
    if (token.startsWith(AuthConstants.BEARER_PREFIX)) {
      token = token.substring(AuthConstants.BEARER_PREFIX.length());
    }

    // Redis에 토큰 블랙리스트 추가
    try {
      Claims claims = tokenGenerator.parseToken(token);
      Date expiration = claims.getExpiration();
      long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

      if (ttl > 0) {
        // Redis에 블랙리스트 추가 (토큰을 키로, 값은 "blacklisted"로 설정)
        redisTemplate.opsForValue().set(AuthConstants.BLACKLIST_PREFIX + token, "blacklisted", ttl,
            TimeUnit.SECONDS);
        log.info("로그아웃 성공: 토큰이 블랙리스트에 추가됨 (만료 시간: {}초)", ttl);
      } else {
        log.info("로그아웃: 이미 만료된 토큰");
      }
    } catch (Exception e) {
      // 토큰 파싱 실패 시 무시 (이미 만료된 토큰일 수 있음)
      log.warn("로그아웃 처리 중 예외 발생: {}", e.getMessage());
    }
  }

  @Transactional
  public void deleteAccount(Long accountId) {
    log.info("계정 삭제 시도: ID: {}", accountId);

    // 계정 존재 확인
    boolean exists = accountRepository.findByAccountId(accountId).isPresent();
    if (!exists) {
      log.warn("계정 삭제 실패: 계정을 찾을 수 없음 - ID: {}", accountId);
      throw new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND);
    }

    // 계정 상태를 DELETED로 변경
    accountRepository.updateStatus(accountId, AccountStatus.DELETED);
    log.info("계정 삭제 성공: ID: {}", accountId);
  }
}
