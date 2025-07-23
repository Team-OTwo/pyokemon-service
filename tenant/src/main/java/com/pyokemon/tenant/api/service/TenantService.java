package com.pyokemon.tenant.api.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.request.LoginRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdatePasswordRequestDto;
import com.pyokemon.tenant.api.dto.request.UpdateProfileRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import com.pyokemon.tenant.api.repository.TenantRepository;
import com.pyokemon.tenant.exception.TenantException;
import com.pyokemon.tenant.mapper.TenantMapper;
import com.pyokemon.tenant.secret.jwt.TokenGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TenantService {

  private final TenantRepository tenantRepository;
  private final TenantMapper tenantMapper;
  private final TokenGenerator tokenGenerator;
  private final PasswordEncoder passwordEncoder;

  // 전체 테넌트 리스트 조회 - Admin
  public TenantListResponseDto getAllTenants() {
    // 1. Repository에서 전체 데이터 조회
    List<Tenant> tenants = tenantRepository.findAll();

    // 2. DTO 변환
    return tenantMapper.toListResponseDto(tenants);
  }

  // 특정 테넌트 상세 조회
  public TenantDetailResponseDto getTenantById(Long id) {
    // 1. 유효성 검증
    if (id == null) {
      throw new TenantException("테넌트 ID는 필수입니다", "TENANT_ID_REQUIRED");
    }
    // 2. Repository에서 데이터 조회
    Tenant tenant = tenantRepository.findById(id).orElseThrow(TenantException::notFound);
    // 3. DTO 변환 및 반환
    return tenantMapper.toResponseDto(tenant);
  }

  // 테넌트 등록 - Admin
  @Transactional
  public TenantDetailResponseDto createTenant(CreateTenantRequestDto request) {
    // 1. 로그인 ID 중복 체크
    if (tenantRepository.existsByLoginId(request.getLoginId())) {
      throw new TenantException("이미 존재하는 아이디입니다", "LOGIN_ID_DUPLICATED");
    }

    // 2. 사업자번호 중복 체크
    if (tenantRepository.existsByCorpId(request.getBusinessNumber())) {
      throw new TenantException("이미 등록된 사업자번호입니다", "BUSINESS_NUMBER_DUPLICATED");
    }

    // 3. 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 4. 엔티티 생성 및 저장
    Tenant tenant = tenantMapper.toEntity(request, encodedPassword);
    tenantRepository.insert(tenant);

    // 5. 저장된 데이터 DTO 변환 후 반환
    return tenantMapper.toResponseDto(tenant);
  }

  // 프로필 수정
  @Transactional
  public TenantDetailResponseDto updateProfile(Long tenantId, UpdateProfileRequestDto request) {
    // 1. 유효성 검증
    if (tenantId == null) {
      throw new TenantException("테넌트 ID는 필수입니다", "TENANT_ID_REQUIRED");
    }

    // 2. 기존 테넌트 조회
    Tenant existingTenant =
        tenantRepository.findById(tenantId).orElseThrow(TenantException::notFound);

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
    Tenant savedTenant = tenantRepository.findById(tenantId).orElseThrow(TenantException::notFound);

    return tenantMapper.toResponseDto(savedTenant);
  }

  // 비밀번호 변경
  @Transactional
  public void changePassword(Long tenantId, UpdatePasswordRequestDto request) {
    // 1. 유효성 검증
    if (tenantId == null) {
      throw new TenantException("테넌트 ID는 필수입니다", "TENANT_ID_REQUIRED");
    }

    if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
      throw new TenantException("현재 비밀번호는 필수입니다", "OLD_PASSWORD_REQUIRED");
    }

    if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
      throw new TenantException("새 비밀번호는 필수입니다", "NEW_PASSWORD_REQUIRED");
    }

    // 2. 기존 테넌트 조회
    Tenant existingTenant =
        tenantRepository.findById(tenantId).orElseThrow(TenantException::notFound);

    // 3. 현재 비밀번호 검증
    if (!passwordEncoder.matches(request.getCurrentPassword(), existingTenant.getPassword())) {
      throw new TenantException("현재 비밀번호가 일치하지 않습니다", "CURRENT_PASSWORD_MISMATCH");
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
  public String login(LoginRequestDto request) {
    // 1. 유효성 검증
    if (request.getLoginId() == null || request.getLoginId().trim().isEmpty()) {
      throw new TenantException("아이디는 필수입니다", "LOGIN_ID_REQUIRED");
    }

    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
      throw new TenantException("비밀번호는 필수입니다", "PASSWORD_REQUIRED");
    }

    // 2. 사용자 조회
    Tenant tenant = tenantRepository.findByLoginId(request.getLoginId())
        .orElseThrow(TenantException::loginFailed);

    // 3. 비밀번호 검증
    if (!passwordEncoder.matches(request.getPassword(), tenant.getPassword())) {
      throw TenantException.loginFailed();
    }

    // 4. JWT 토큰 생성 및 반환
    // Access Token만 발급 (WEB 디바이스 타입으로 설정)
    return tokenGenerator.generateAccessToken(tenant.getLoginId(), "WEB").getAccess().getToken();
  }

  // 로그아웃
  public void logout(String token) {
    // Gateway에서 이미 토큰 검증을 완료했으므로 여기서는 비즈니스 로직만 처리

    // TODO: 필요시 토큰 블랙리스트 추가
    log.info("로그아웃");
  }

  // 테넌트 삭제 - Admin
  @Transactional
  public void deleteTenant(Long id) {
    // 1. 유효성 검증
    if (id == null) {
      throw new TenantException("테넌트 ID는 필수입니다", "TENANT_ID_REQUIRED");
    }

    // 2. 삭제할 테넌트가 존재하는지 확인
    if (!tenantRepository.existsById(id)) {
      throw TenantException.notFound();
    }

    // 3. 테넌트 삭제
    try {
      tenantRepository.deleteById(id);
    } catch (Exception e) {
      throw new TenantException("테넌트 삭제에 실패했습니다", "TENANT_DELETE_FAILED", e);
    }
  }


}
