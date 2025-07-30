package com.pyokemon.account.tenant.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.account.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.account.tenant.dto.request.TenantRegisterRequestDto;
import com.pyokemon.account.tenant.dto.request.UpdateTenantProfileRequestDto;
import com.pyokemon.account.tenant.dto.response.TenantListResponseDto;
import com.pyokemon.account.tenant.dto.response.TenantProfileResponseDto;
import com.pyokemon.account.tenant.service.TenantService;
import com.pyokemon.common.dto.ResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

  private final TenantService tenantService;

  // 테넌트 등록
  @PostMapping
  public ResponseEntity<ResponseDto<TenantProfileResponseDto>> registerTenant(
      @Valid @RequestBody TenantRegisterRequestDto request) {
    TenantProfileResponseDto response = tenantService.registerTenant(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseDto.success(response, "테넌트 등록 성공"));
  }

  // 전체 테넌트 목록 조회 (관리자 전용)
  @GetMapping
  public ResponseEntity<ResponseDto<TenantListResponseDto>> getAllTenants() {
    TenantListResponseDto response = tenantService.getAllTenants();
    return ResponseEntity.ok(ResponseDto.success(response, "테넌트 목록 조회 성공"));
  }

  // 특정 테넌트 상세 조회 (관리자 전용)
  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<TenantProfileResponseDto>> getTenantById(
      @PathVariable Long id) {
    TenantProfileResponseDto response = tenantService.getTenantProfile(id);
    return ResponseEntity.ok(ResponseDto.success(response, "테넌트 상세 조회 성공"));
  }

  // 테넌트 삭제 (관리자 전용)
  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseDto<Void>> deleteTenant(@PathVariable Long id) {
    tenantService.deleteTenant(id);
    return ResponseEntity.ok(ResponseDto.success("테넌트 삭제 성공"));
  }

  // 내 정보 조회 (테넌트 본인만)
  @GetMapping("/profile")
  public ResponseEntity<ResponseDto<TenantProfileResponseDto>> getMyProfile() {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long accountId = Long.parseLong(currentUserAccountId);
    TenantProfileResponseDto response =
        tenantService.getMyTenantProfile(accountId, currentUserAccountId);
    return ResponseEntity.ok(ResponseDto.success(response, "내 정보 조회 성공"));
  }

  // 정보 수정 (테넌트 본인만)
  @PutMapping("/profile")
  public ResponseEntity<ResponseDto<TenantProfileResponseDto>> updateProfile(
      @Valid @RequestBody UpdateTenantProfileRequestDto request) {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long accountId = Long.parseLong(currentUserAccountId);
    TenantProfileResponseDto response =
        tenantService.updateMyTenantProfile(accountId, request, currentUserAccountId);
    return ResponseEntity.ok(ResponseDto.success(response, "내 정보 수정 성공"));
  }

  // 테넌트 계정 삭제 (테넌트 본인만)
  @DeleteMapping("/profile")
  public ResponseEntity<ResponseDto<Void>> deleteMyAccount() {
    String currentUserAccountId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    Long accountId = Long.parseLong(currentUserAccountId);
    tenantService.deleteMyTenantAccount(accountId, currentUserAccountId);
    return ResponseEntity.ok(ResponseDto.success("테넌트 계정 삭제 성공"));
  }
}
