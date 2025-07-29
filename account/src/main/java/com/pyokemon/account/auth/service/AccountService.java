package com.pyokemon.account.auth.service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;

  @Transactional
  public LoginResponseDto login(LoginRequestDto request) {
    // TODO: 구현 필요
    return LoginResponseDto.builder().accessToken("dummy-token").refreshToken("dummy-refresh-token")
        .role("USER").accountId(1L).build();
  }

  @Transactional
  public TokenResponseDto refreshToken(String refreshToken) {
    // TODO: 구현 필요
    return TokenResponseDto.builder().accessToken("dummy-new-token").build();
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
