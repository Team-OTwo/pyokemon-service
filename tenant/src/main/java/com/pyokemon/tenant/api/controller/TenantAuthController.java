package com.pyokemon.tenant.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.service.TenantService;
import com.pyokemon.tenant.secret.jwt.dto.TokenDto;
import com.pyokemon.tenant.web.context.GatewayRequestHeaderUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TenantAuthController {

  private final TenantService tenantService;

  // 로그인 /tenants/auth/login
  @PostMapping("/auth/login")
  public ResponseEntity<ResponseDto<TokenDto.AccessRefreshToken>> login(
      @Valid @RequestBody LoginRequestDto request) {
    TokenDto.AccessRefreshToken tokens = tenantService.login(request);
    return ResponseEntity.ok(ResponseDto.success(tokens, "로그인 성공"));
  }

  // 로그아웃 /tenants/auth/logout
  @PostMapping("/auth/logout")
  public ResponseEntity<ResponseDto<Void>> logout() {
    String token = GatewayRequestHeaderUtils.getBearerTokenOrThrowException();
    tenantService.logout(token);
    return ResponseEntity.ok(ResponseDto.success("로그아웃 성공"));
  }

  // 토큰 갱신 /tenants/auth/refresh
  @PostMapping("/auth/refresh")
  public ResponseEntity<ResponseDto<TokenDto.AccessToken>> refresh(
      @Valid @RequestBody TokenDto.RefreshRequest request) {
    TokenDto.AccessToken newToken = tenantService.refresh(request);
    return ResponseEntity.ok(ResponseDto.success(newToken, "토큰 갱신 성공"));
  }
}
