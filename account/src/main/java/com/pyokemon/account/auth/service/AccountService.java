package com.pyokemon.account.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;

  @Transactional
  public LoginResponseDto login(LoginRequestDto request) {
    // 계정 조회
    Optional<Account> accountOpt = accountRepository.findByLoginId(request.getLoginId());
    if (accountOpt.isEmpty()) {
      throw new BusinessException("로그인 ID 또는 비밀번호가 올바르지 않습니다.", AccountErrorCodes.INVALID_LOGIN);
    }

    Account account = accountOpt.get();
    
    // 계정 상태 확인
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new BusinessException("삭제된 계정입니다.", AccountErrorCodes.ACCOUNT_DELETED);
    }

    // 비밀번호 확인
    if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
      throw new BusinessException("로그인 ID 또는 비밀번호가 올바르지 않습니다.", AccountErrorCodes.INVALID_LOGIN);
    }

    // JWT 토큰 생성
    String accessToken = tokenGenerator.generateAccessToken(account.getAccountId(), account.getRole());
    String refreshToken = tokenGenerator.generateRefreshToken(account.getAccountId(), account.getRole());

    return LoginResponseDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .role(account.getRole())
        .accountId(account.getAccountId())
        .build();
  }

  @Transactional
  public TokenResponseDto refreshToken(String refreshToken) {
    try {
      // 토큰 검증 및 파싱
      var claims = tokenGenerator.parseToken(refreshToken);
      Long accountId = Long.parseLong(claims.getSubject());
      String role = claims.get("role", String.class);

      // 새로운 액세스 토큰 생성
      String newAccessToken = tokenGenerator.generateAccessToken(accountId, role);

      return TokenResponseDto.builder()
          .accessToken(newAccessToken)
          .build();
    } catch (Exception e) {
      throw new BusinessException("유효하지 않은 토큰입니다.", AccountErrorCodes.INVALID_TOKEN);
    }
  }

  @Transactional
  public void changePassword(Long accountId, UpdatePasswordRequestDto request) {
    // TODO: 구현 필요
  }

  @Transactional
  public void deleteAccount(Long accountId) {
    // TODO: 구현 필요
  }
}
