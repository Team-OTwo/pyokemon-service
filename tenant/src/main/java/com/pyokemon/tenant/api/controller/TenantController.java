package com.pyokemon.tenant.api.controller;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.tenant.annotation.AdminOnly;
import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    //전체 테넌트 리스트 조회 GET /api/tenants
    @AdminOnly
    @GetMapping
    public ResponseEntity<ResponseDto<TenantListResponseDto>> getAllTenants() {
        TenantListResponseDto response = tenantService.getAllTenants();
        return ResponseEntity.ok(ResponseDto.success(response, "테넌트 리스트 조회 성공"));
    }

    //특정 테넌트 상세 조회 GET /api/tenants/{id}
    @AdminOnly
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TenantDetailResponseDto>> getTenantById(@PathVariable Long id) {
        TenantDetailResponseDto response = tenantService.getTenantById(id);
        return ResponseEntity.ok(ResponseDto.success(response, "테넌트 상세 조회 성공"));
    }

    //테넌트 등록 POST /api/tenants
    @AdminOnly
    @PostMapping
    public ResponseEntity<ResponseDto<TenantDetailResponseDto>> createTenant(
            @Valid @RequestBody CreateTenantRequestDto request) {
        TenantDetailResponseDto response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.success(response, "테넌트 등록 성공"));
    }

    //테넌트 삭제 DELETE /api/tenants/{id}
    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<Void>> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.ok(ResponseDto.success("테넌트 삭제 성공"));
    }

    //로그인 POST /api/tenants/login
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<String>> login(@Valid @RequestBody LoginRequestDto request) {
        String token = tenantService.login(request);
        return ResponseEntity.ok(ResponseDto.success(token, "로그인 성공"));
    }

    //로그아웃 /api/tenant/logout
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        // "Bearer " 접두사 제거
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        tenantService.logout(token);
        return ResponseEntity.ok(ResponseDto.success("로그아웃 성공"));
    }

    //내 정보 조회  /api/tenant/profile
    @GetMapping("/profile")
    public ResponseEntity<ResponseDto<TenantDetailResponseDto>> getMyProfile(
            @RequestHeader("Authorization") String authHeader) {
        // TODO: JWT 토큰에서 사용자 ID 추출하는 유틸 메서드 필요
        // 현재는 임시로 하드코딩 (실제로는 토큰 파싱 필요)
        Long currentTenantId = getCurrentTenantIdFromToken(authHeader);
        
        TenantDetailResponseDto response = tenantService.getTenantById(currentTenantId);
        return ResponseEntity.ok(ResponseDto.success(response, "내 정보 조회 성공"));
    }

    //정보 수정 /api/tenant/profile
    @PutMapping("/profile")
    public ResponseEntity<ResponseDto<TenantDetailResponseDto>> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        Long currentTenantId = getCurrentTenantIdFromToken(authHeader);
        
        TenantDetailResponseDto response = tenantService.updateProfile(currentTenantId, request);
        return ResponseEntity.ok(ResponseDto.success(response, "내 정보 수정 성공"));
    }


    //비밀번호 변경 /api/tenant/password
    @PatchMapping("/password")
    public ResponseEntity<ResponseDto<Void>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdatePasswordRequestDto request) {
        Long currentTenantId = getCurrentTenantIdFromToken(authHeader);
        
        tenantService.changePassword(currentTenantId, request);
        return ResponseEntity.ok(ResponseDto.success("비밀번호 변경 성공"));
    }

    //JWT 토큰에서 사용자 ID 추출 -> 아직 구현 x
    private Long getCurrentTenantIdFromToken(String authHeader) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        // TODO: TokenGenerator를 사용해서 토큰에서 loginId 추출
        // TODO: Repository를 사용해서 loginId로 Tenant ID 조회
        // 현재는 임시로 1L 반환
        return 1L;
    }
}
