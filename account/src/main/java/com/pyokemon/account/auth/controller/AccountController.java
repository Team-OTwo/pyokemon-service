package com.pyokemon.account.auth.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.auth.dto.request.LoginRequestDto;
import com.pyokemon.account.auth.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.account.auth.dto.response.LoginResponseDto;
import com.pyokemon.account.auth.dto.response.TokenResponseDto;
import com.pyokemon.account.auth.service.AccountService;
import com.pyokemon.account.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  // 통합 로그인
  @PostMapping("/login")
  public ResponseEntity<ResponseDto<LoginResponseDto>> login(
      @Valid @RequestBody LoginRequestDto request) {
    LoginResponseDto response = accountService.login(request);
    return ResponseEntity.ok(ResponseDto.success(response, "로그인 성공"));
  }

  // 통합 로그아웃
  @PostMapping("/logout")
  public ResponseEntity<ResponseDto<Void>> logout(
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    accountService.logout(authHeader);
    return ResponseEntity.ok(ResponseDto.success("로그아웃 성공"));
  }

  // Access Token 갱신
  @PostMapping("/refresh")
  public ResponseEntity<ResponseDto<TokenResponseDto>> refreshToken(
      @RequestHeader("Authorization") String authHeader) {
    String refreshToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    TokenResponseDto response = accountService.refreshToken(refreshToken);
    return ResponseEntity.ok(ResponseDto.success(response, "토큰 갱신 성공"));
  }

  // 비밀번호 변경
  @PutMapping("/password")
  public ResponseEntity<ResponseDto<Void>> changePassword(
      @Valid @RequestBody UpdatePasswordRequestDto request) {
    // 현재 로그인한 사용자의 ID를 가져옴 (헤더에서 토큰 추출 후 파싱)
    Long accountId = getCurrentAccountId();
    accountService.changePassword(accountId, request);
    return ResponseEntity.ok(ResponseDto.success("비밀번호 변경 성공"));
  }

  // 현재 로그인한 사용자의 ID를 가져오는 헬퍼 메소드
  private Long getCurrentAccountId() {
    try {
      String userId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
      return Long.parseLong(userId);
    } catch (Exception e) {
      throw new BusinessException(AccountErrorCodes.ACCESS_DENIED, "인증 정보가 없습니다.");
    }
  }

  // 현재 로그인한 사용자의 역할을 가져오는 헬퍼 메소드
  private String getCurrentUserRole() {
    try {
      return GatewayRequestHeaderUtils.getUserRoleOrThrowException();
    } catch (Exception e) {
      throw new BusinessException(AccountErrorCodes.ACCESS_DENIED, "인증 정보가 없습니다.");
    }
  }
}
