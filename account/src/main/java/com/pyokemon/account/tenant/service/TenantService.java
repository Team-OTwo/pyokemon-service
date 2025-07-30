package com.pyokemon.account.tenant.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.entity.AccountStatus;
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
    // 로그인 ID 중복 체크
    if (accountRepository.findByLoginId(request.getLoginId()).isPresent()) {
      throw new BusinessException("이미 사용 중인 로그인 ID입니다.", AccountErrorCodes.DUPLICATE_LOGIN_ID);
    }
    // 사업자번호 중복 체크
    if (tenantRepository.findByCorpId(request.getCorpId()).isPresent()) {
      throw new BusinessException("이미 등록된 사업자번호입니다.", AccountErrorCodes.DUPLICATE_CORP_ID);
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    Account account = Account.builder().loginId(request.getLoginId()).password(encodedPassword)
        .role("TENANT").status(AccountStatus.ACTIVE).build();

    accountRepository.insert(account);

    Tenant tenant = Tenant.builder().accountId(account.getAccountId()).name(request.getName())
        .corpId(request.getCorpId()).city(request.getCity()).street(request.getStreet())
        .zipcode(request.getZipcode()).ceo(request.getCeo()).build();

    tenantRepository.insert(tenant);

    return tenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getTenantProfile(Long tenantId) {
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    Account account = accountRepository.findByAccountId(tenant.getAccountId()).orElseThrow(
        () -> new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND));

    return tenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public TenantProfileResponseDto updateTenantProfile(Long tenantId,
      UpdateTenantProfileRequestDto request) {
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    Account account = accountRepository.findByAccountId(tenant.getAccountId()).orElseThrow(
        () -> new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND));

    Tenant updatedTenant = tenant.updateFromRequest(request);

    tenantRepository.update(updatedTenant);

    return updatedTenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public void deleteTenant(Long tenantId) {
    // 테넌트 존재 확인
    Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    // Account 상태를 DELETED로 변경
    accountRepository.updateStatus(tenant.getAccountId(), AccountStatus.DELETED);

    tenantRepository.delete(tenantId);
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getMyTenantProfile(Long accountId, String currentUserAccountId) {
    // 권한 검증
    if (!accountId.toString().equals(currentUserAccountId)) {
      throw new BusinessException("자신의 정보만 조회할 수 있습니다.", AccountErrorCodes.ACCESS_DENIED);
    }

    Tenant tenant = tenantRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    Account account = accountRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND));

    return tenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public TenantProfileResponseDto updateMyTenantProfile(Long accountId,
      UpdateTenantProfileRequestDto request, String currentUserAccountId) {
    // 권한 검증
    if (!accountId.toString().equals(currentUserAccountId)) {
      throw new BusinessException("자신의 정보만 수정할 수 있습니다.", AccountErrorCodes.ACCESS_DENIED);
    }

    Tenant tenant = tenantRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    Account account = accountRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("계정을 찾을 수 없습니다.", AccountErrorCodes.ACCOUNT_NOT_FOUND));

    Tenant updatedTenant = tenant.updateFromRequest(request);
    tenantRepository.update(updatedTenant);

    return updatedTenant.toTenantProfileResponseDto(account.getLoginId());
  }

  @Transactional
  public void deleteMyTenantAccount(Long accountId, String currentUserAccountId) {
    // 권한 검증
    if (!accountId.toString().equals(currentUserAccountId)) {
      throw new BusinessException("자신의 계정만 삭제할 수 있습니다.", AccountErrorCodes.ACCESS_DENIED);
    }

    Tenant tenant = tenantRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));

    // Soft Delete
    accountRepository.updateStatus(accountId, AccountStatus.DELETED);
    tenantRepository.delete(tenant.getTenantId());
  }

  @Transactional(readOnly = true)
  public TenantListResponseDto getAllTenants() {
    List<Tenant> tenants = tenantRepository.findAll();

    List<TenantListResponseDto.TenantSummaryDto> tenantSummaries = tenants.stream().map(tenant -> {
      Account account = accountRepository.findByAccountId(tenant.getAccountId()).orElse(null);
      return TenantListResponseDto.TenantSummaryDto.fromTenant(tenant,
          account != null ? account.getLoginId() : null);
    }).collect(Collectors.toList());

    return TenantListResponseDto.builder().tenants(tenantSummaries)
        .totalCount(tenantSummaries.size()).build();
  }
}
