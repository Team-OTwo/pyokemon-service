package com.pyokemon.tenant.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.tenant.annotation.AdminOnly;
import com.pyokemon.tenant.annotation.TenantOnly;
import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import com.pyokemon.tenant.api.repository.TenantRepository;
import com.pyokemon.tenant.api.service.TenantService;
import com.pyokemon.tenant.exception.TenantException;
import com.pyokemon.tenant.web.context.GatewayRequestHeaderUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TenantController {

  private final TenantService tenantService;
  private final TenantRepository tenantRepository;

  // 전체 테넌트 리스트 조회 GET /tenants
  @AdminOnly
  @GetMapping
  public ResponseEntity<ResponseDto<TenantListResponseDto>> getAllTenants() {
    TenantListResponseDto response = tenantService.getAllTenants();
    return ResponseEntity.ok(ResponseDto.success(response, "테넌트 리스트 조회 성공"));
  }

  // 특정 테넌트 상세 조회 GET /tenants/{id}
  @AdminOnly
  @GetMapping("/{id}")
  public ResponseEntity<ResponseDto<TenantDetailResponseDto>> getTenantById(@PathVariable Long id) {
    TenantDetailResponseDto response = tenantService.getTenantById(id);
    return ResponseEntity.ok(ResponseDto.success(response, "테넌트 상세 조회 성공"));
  }

  // 테넌트 등록 POST /tenants
  @AdminOnly
  @PostMapping
  public ResponseEntity<ResponseDto<TenantDetailResponseDto>> createTenant(
      @Valid @RequestBody CreateTenantRequestDto request) {
    TenantDetailResponseDto response = tenantService.createTenant(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ResponseDto.success(response, "테넌트 등록 성공"));
  }

  // 테넌트 삭제 DELETE /tenants/{id}
  @AdminOnly
  @DeleteMapping("/{id}")
  public ResponseEntity<ResponseDto<Void>> deleteTenant(@PathVariable Long id) {
    tenantService.deleteTenant(id);
    return ResponseEntity.ok(ResponseDto.success("테넌트 삭제 성공"));
  }



  // 내 정보 조회 /tenants/profile
  @TenantOnly
  @GetMapping("/profile")
  public ResponseEntity<ResponseDto<TenantDetailResponseDto>> getMyProfile() {
    Long currentTenantId = getCurrentTenantId();

    TenantDetailResponseDto response = tenantService.getTenantById(currentTenantId);
    return ResponseEntity.ok(ResponseDto.success(response, "내 정보 조회 성공"));
  }

  // 정보 수정 /tenants/profile
  @TenantOnly
  @PutMapping("/profile")
  public ResponseEntity<ResponseDto<TenantDetailResponseDto>> updateProfile(
      @Valid @RequestBody UpdateProfileRequestDto request) {
    Long currentTenantId = getCurrentTenantId();

    TenantDetailResponseDto response = tenantService.updateProfile(currentTenantId, request);
    return ResponseEntity.ok(ResponseDto.success(response, "내 정보 수정 성공"));
  }


  // 비밀번호 변경 /tenants/password
  @TenantOnly
  @PatchMapping("/password")
  public ResponseEntity<ResponseDto<Void>> changePassword(
      @Valid @RequestBody UpdatePasswordRequestDto request) {
    Long currentTenantId = getCurrentTenantId();

    tenantService.changePassword(currentTenantId, request);
    return ResponseEntity.ok(ResponseDto.success("비밀번호 변경 성공"));
  }

  // Gateway에서 전달받은 헤더에서 현재 사용자 ID 추출
  private Long getCurrentTenantId() {
    try {
      // Gateway에서 X-Auth-UserId 헤더로 사용자 정보 전달 (이미 JWT 검증 완료)
      String loginId = GatewayRequestHeaderUtils.getUserIdOrThrowException();

      // Repository에서 loginId로 Tenant 조회
      Tenant tenant =
          tenantRepository.findByLoginId(loginId).orElseThrow(TenantException::notFound);

      return tenant.getId();

    } catch (Exception e) {
      // 헤더에서 사용자 정보 추출 실패 시 접근 거부
      throw TenantException.accessDenied();
    }
  }
}
