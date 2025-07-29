package com.pyokemon.tenant.api.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.TenantErrorCode;
import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import com.pyokemon.tenant.api.repository.TenantRepository;
import com.pyokemon.tenant.mapper.TenantConverter;
import com.pyokemon.tenant.secret.jwt.TokenGenerator;
import com.pyokemon.tenant.secret.jwt.dto.TokenDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TenantService {

  private final TenantRepository tenantRepository;
  private final TenantConverter tenantConverter;
  private final TokenGenerator tokenGenerator;
  private final PasswordEncoder passwordEncoder;

  // 전체 테넌트 리스트 조회 - Admin
  public TenantListResponseDto getAllTenants() {
    // 1. Repository에서 전체 데이터 조회
    List<Tenant> tenants = tenantRepository.findAll();

    // 2. DTO 변환
    return tenantConverter.toListResponseDto(tenants);
  }

  // 특정 테넌트 상세 조회
  public TenantDetailResponseDto getTenantById(Long id) {
    // 1. 유효성 검증
    if (id == null) {
      throw new BusinessException(TenantErrorCode.TENANT_ID_REQUIRED.getMessage(),
          TenantErrorCode.TENANT_ID_REQUIRED.getCode());
    }
    // 2. Repository에서 데이터 조회
    Tenant tenant = tenantRepository.findById(id)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND.getMessage(),
            TenantErrorCode.TENANT_NOT_FOUND.getCode()));
    // 3. DTO 변환 및 반환
    return tenantConverter.toResponseDto(tenant);
  }

  // 테넌트 등록 - Admin
  @Transactional
  public TenantDetailResponseDto createTenant(CreateTenantRequestDto request) {
    // 1. 로그인 ID 중복 체크
    if (tenantRepository.existsByLoginId(request.getLoginId())) {
      throw new BusinessException(TenantErrorCode.LOGIN_ID_DUPLICATED.getMessage(),
          TenantErrorCode.LOGIN_ID_DUPLICATED.getCode());
    }

    // 2. 사업자번호 중복 체크
    if (tenantRepository.existsByCorpId(request.getCorpId())) {
      throw new BusinessException(TenantErrorCode.BUSINESS_NUMBER_DUPLICATED.getMessage(),
          TenantErrorCode.BUSINESS_NUMBER_DUPLICATED.getCode());
    }

    // 3. 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 4. 엔티티 생성 및 저장
    Tenant tenant = tenantConverter.toEntity(request, encodedPassword);
    tenantRepository.insert(tenant);

    // 5. 저장된 데이터 DTO 변환 후 반환
    return tenantConverter.toResponseDto(tenant);
  }

  // 프로필 수정
  @Transactional
  public TenantDetailResponseDto updateProfile(Long tenantId, UpdateProfileRequestDto request) {
    // 1. 유효성 검증
    if (tenantId == null) {
      throw new BusinessException(TenantErrorCode.TENANT_ID_REQUIRED.getMessage(),
          TenantErrorCode.TENANT_ID_REQUIRED.getCode());
    }

    // 2. 기존 테넌트 조회
    Tenant existingTenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND.getMessage(),
            TenantErrorCode.TENANT_NOT_FOUND.getCode()));

    // 3. 수정할 정보 업데이트 (비밀번호와 로그인ID, 사업자번호는 수정 불가)
    Tenant updatedTenant =
        Tenant.builder().id(existingTenant.getId()).loginId(existingTenant.getLoginId()) // 수정 불가
            .password(existingTenant.getPassword()) // 수정 불가
            .corpId(existingTenant.getCorpId()) // 수정 불가 (사업자번호)
            .corpName(request.getCorpName()) // 수정 가능
            .city(request.getCity()) // 수정 가능
            .street(request.getStreet()) // 수정 가능
            .zipcode(request.getZipcode()) // 수정 가능
            .ceoName(request.getCeoName()) // 수정 가능
            .createdAt(existingTenant.getCreatedAt()) // 유지
            .updatedAt(existingTenant.getUpdatedAt()) // DB에서 자동 업데이트
            .build();

    // 4. 데이터베이스 업데이트
    tenantRepository.update(updatedTenant);

    // 5. 업데이트된 데이터 조회 및 DTO 변환
    Tenant savedTenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND.getMessage(),
            TenantErrorCode.TENANT_NOT_FOUND.getCode()));

    return tenantConverter.toResponseDto(savedTenant);
  }

  // 비밀번호 변경
  @Transactional
  public void changePassword(Long tenantId, UpdatePasswordRequestDto request) {
    // 1. 유효성 검증
    if (tenantId == null) {
      throw new BusinessException(TenantErrorCode.TENANT_ID_REQUIRED.getMessage(),
          TenantErrorCode.TENANT_ID_REQUIRED.getCode());
    }

    if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.OLD_PASSWORD_REQUIRED.getMessage(),
          TenantErrorCode.OLD_PASSWORD_REQUIRED.getCode());
    }

    if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.NEW_PASSWORD_REQUIRED.getMessage(),
          TenantErrorCode.NEW_PASSWORD_REQUIRED.getCode());
    }

    // 2. 기존 테넌트 조회
    Tenant existingTenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND.getMessage(),
            TenantErrorCode.TENANT_NOT_FOUND.getCode()));

    // 3. 현재 비밀번호 검증
    if (!passwordEncoder.matches(request.getCurrentPassword(), existingTenant.getPassword())) {
      throw new BusinessException(TenantErrorCode.CURRENT_PASSWORD_MISMATCH.getMessage(),
          TenantErrorCode.CURRENT_PASSWORD_MISMATCH.getCode());
    }

    // 4. 새 비밀번호 암호화
    String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

    // 5. 비밀번호만 업데이트
    Tenant updatedTenant = Tenant.builder().id(existingTenant.getId())
        .loginId(existingTenant.getLoginId()).password(encodedNewPassword) // 새 비밀번호
        .corpName(existingTenant.getCorpName()).corpId(existingTenant.getCorpId())
        .city(existingTenant.getCity()).street(existingTenant.getStreet())
        .zipcode(existingTenant.getZipcode()).ceoName(existingTenant.getCeoName())
        .createdAt(existingTenant.getCreatedAt()).updatedAt(existingTenant.getUpdatedAt()) // DB에서
                                                                                           // 자동
                                                                                           // 업데이트
        .build();

    // 6. 데이터베이스 업데이트
    tenantRepository.update(updatedTenant);
  }

  // 로그인
  public TokenDto.AccessRefreshToken login(LoginRequestDto request) {
    // 1. 유효성 검증
    if (request.getLoginId() == null || request.getLoginId().trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.LOGIN_ID_REQUIRED.getMessage(),
          TenantErrorCode.LOGIN_ID_REQUIRED.getCode());
    }

    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.PASSWORD_REQUIRED.getMessage(),
          TenantErrorCode.PASSWORD_REQUIRED.getCode());
    }

    // 2. 사용자 조회
    Tenant tenant = tenantRepository.findByLoginId(request.getLoginId())
        .orElseThrow(() -> new BusinessException(TenantErrorCode.LOGIN_FAILED.getMessage(),
            TenantErrorCode.LOGIN_FAILED.getCode()));

    // 3. 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), tenant.getPassword())) {
      throw new BusinessException(TenantErrorCode.LOGIN_FAILED.getMessage(),
          TenantErrorCode.LOGIN_FAILED.getCode());
    }

    // 4. JWT 토큰 생성 및 반환 (Access + Refresh 토큰)
    return tokenGenerator.generateAccessRefreshToken(tenant.getLoginId(), "WEB");
  }

  // 로그아웃
  public void logout(String token) {
    // Gateway에서 이미 토큰 검증을 완료했으므로 여기서는 비즈니스 로직만 처리

    // TODO: 토큰 블랙리스트 추가
    log.info("로그아웃");
  }

  // 토큰 갱신
  public TokenDto.AccessToken refresh(TokenDto.RefreshRequest request) {
    // 1. 유효성 검증
    if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
      throw new BusinessException(TenantErrorCode.REFRESH_TOKEN_REQUIRED.getMessage(),
          TenantErrorCode.REFRESH_TOKEN_REQUIRED.getCode());
    }

    // 2. 리프레시 토큰 검증 및 사용자 ID 추출
    String loginId = tokenGenerator.validateJwtToken(request.getRefreshToken());
    if (loginId == null) {
      throw new BusinessException(TenantErrorCode.INVALID_REFRESH_TOKEN.getMessage(),
          TenantErrorCode.INVALID_REFRESH_TOKEN.getCode());
    }

    // 3. 사용자 존재 여부 확인
    Tenant tenant = tenantRepository.findByLoginId(loginId)
        .orElseThrow(() -> new BusinessException(TenantErrorCode.USER_NOT_FOUND.getMessage(),
            TenantErrorCode.USER_NOT_FOUND.getCode()));

    // 4. 새로운 Access 토큰 발급
    return tokenGenerator.generateAccessToken(tenant.getLoginId(), "WEB");
  }

  // 테넌트 삭제 - Admin
  @Transactional
  public void deleteTenant(Long id) {
    // 1. 유효성 검증
    if (id == null) {
      throw new BusinessException(TenantErrorCode.TENANT_ID_REQUIRED.getMessage(),
          TenantErrorCode.TENANT_ID_REQUIRED.getCode());
    }

    // 2. 삭제할 테넌트가 존재하는지 확인
    if (!tenantRepository.existsById(id)) {
      throw new BusinessException(TenantErrorCode.TENANT_NOT_FOUND.getMessage(),
          TenantErrorCode.TENANT_NOT_FOUND.getCode());
    }

    // 3. 테넌트 삭제
    try {
      tenantRepository.deleteById(id);
    } catch (Exception e) {
      throw new BusinessException(TenantErrorCode.TENANT_DELETE_FAILED.getMessage(),
          TenantErrorCode.TENANT_DELETE_FAILED.getCode(), e);
    }
  }


}
