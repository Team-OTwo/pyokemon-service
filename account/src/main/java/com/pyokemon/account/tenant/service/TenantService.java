package com.pyokemon.account.tenant.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.repository.AccountRepository;
import com.pyokemon.account.tenant.dto.request.TenantRegisterRequestDto;
import com.pyokemon.account.tenant.dto.request.UpdateTenantProfileRequestDto;
import com.pyokemon.account.tenant.dto.response.TenantListResponseDto;
import com.pyokemon.account.tenant.dto.response.TenantProfileResponseDto;
import com.pyokemon.account.tenant.entity.Tenant;
import com.pyokemon.account.tenant.repository.TenantRepository;
import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {

  private final TenantRepository tenantRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public TenantProfileResponseDto registerTenant(TenantRegisterRequestDto request) {
    // 1. 로그인 ID 중복 체크
    if (accountRepository.findByLoginId(request.getLoginId()).isPresent()) {
      throw new BusinessException(AccountErrorCodes.DUPLICATE_LOGIN_ID, "이미 사용 중인 로그인 ID입니다.");
    }

    // 2. 사업자번호 중복 체크
    if (tenantRepository.findByCorpId(request.getCorpId()).isPresent()) {
      throw new BusinessException(AccountErrorCodes.DUPLICATE_CORP_ID, "이미 등록된 사업자번호입니다.");
    }

    // 3. Account 생성
    String encodedPassword = passwordEncoder.encode(request.getPassword());
    Account account = Account.builder().loginId(request.getLoginId()).password(encodedPassword)
        .role("TENANT").status("ACTIVE").build();

    accountRepository.insert(account);

    // 4. Tenant 생성
    Tenant tenant = Tenant.builder().accountId(account.getAccountId()).name(request.getName())
        .corpId(request.getCorpId()).city(request.getCity()).street(request.getStreet())
        .zipcode(request.getZipcode()).ceo(request.getCeo()).build();

    tenantRepository.insert(tenant);

    // 5. 응답 생성
    return tenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getTenantProfile(Long tenantId) {
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다."));

    Account account = accountRepository.findByAccountId(tenant.getAccountId()).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.ACCOUNT_NOT_FOUND, "계정을 찾을 수 없습니다."));

    return tenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public TenantProfileResponseDto updateTenantProfile(Long tenantId,
      UpdateTenantProfileRequestDto request) {
    // 1. 테넌트 존재 확인
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다."));

    // 2. 사업자번호 중복 체크 (자신 제외)
    Optional<Tenant> existingTenant = tenantRepository.findByCorpId(request.getCorpId());
    if (existingTenant.isPresent() && !existingTenant.get().getTenantId().equals(tenantId)) {
      throw new BusinessException(AccountErrorCodes.DUPLICATE_CORP_ID, "이미 등록된 사업자번호입니다.");
    }

    // 3. 테넌트 정보 업데이트
    Tenant updatedTenant = Tenant.builder().tenantId(tenant.getTenantId())
        .accountId(tenant.getAccountId()).name(request.getName()).corpId(request.getCorpId())
        .city(request.getCity()).street(request.getStreet()).zipcode(request.getZipcode())
        .ceo(request.getCeo()).build();

    tenantRepository.update(updatedTenant);

    // 4. 업데이트된 테넌트 조회
    Tenant savedTenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다."));

    Account account = accountRepository.findByAccountId(savedTenant.getAccountId()).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.ACCOUNT_NOT_FOUND, "계정을 찾을 수 없습니다."));

    return savedTenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public void deleteTenant(Long tenantId) {
    // 1. 테넌트 존재 확인
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException(AccountErrorCodes.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다."));

    // 2. Account 상태를 DELETED로 변경 (Soft Delete)
    accountRepository.updateStatus(tenant.getAccountId(), "DELETED");

    // 3. 테넌트 정보 삭제
    tenantRepository.delete(tenantId);
  }

  @Transactional(readOnly = true)
  public TenantListResponseDto getAllTenants() {
    List<Tenant> tenants = tenantRepository.findAll();

    List<TenantListResponseDto.TenantSummaryDto> tenantSummaries = tenants.stream().map(tenant -> {
      Account account = accountRepository.findByAccountId(tenant.getAccountId()).orElse(null);
      return TenantListResponseDto.TenantSummaryDto.fromTenant(tenant, account != null ? account.getLoginId() : null);
    }).collect(Collectors.toList());

    return TenantListResponseDto.builder().tenants(tenantSummaries)
        .totalCount(tenantSummaries.size()).build();
  }
}
