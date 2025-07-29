package com.pyokemon.account.tenant.service;

import java.util.List;
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
    // TODO: 구현 필요
    return TenantProfileResponseDto.builder().tenantId(1L).accountId(1L)
        .loginId(request.getLoginId()).name(request.getName()).corpId(request.getCorpId())
        .city(request.getCity()).street(request.getStreet()).zipcode(request.getZipcode())
        .ceo(request.getCeo()).build();
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getTenantProfile(Long tenantId) {
    // TODO: 구현 필요
    return TenantProfileResponseDto.builder().tenantId(tenantId).accountId(1L)
        .loginId("dummy-login-id").name("dummy-name").corpId("dummy-corp-id").city("dummy-city")
        .street("dummy-street").zipcode("dummy-zipcode").ceo("dummy-ceo").build();
  }

  @Transactional
  public TenantProfileResponseDto updateTenantProfile(Long tenantId,
      UpdateTenantProfileRequestDto request) {
    // TODO: 구현 필요
    return TenantProfileResponseDto.builder().tenantId(tenantId).accountId(1L)
        .loginId("dummy-login-id").name(request.getName()).corpId(request.getCorpId())
        .city(request.getCity()).street(request.getStreet()).zipcode(request.getZipcode())
        .ceo(request.getCeo()).build();
  }

  @Transactional
  public void deleteTenant(Long tenantId) {
    // TODO: 구현 필요
  }

  @Transactional(readOnly = true)
  public TenantListResponseDto getAllTenants() {
    // TODO: 구현 필요
    return TenantListResponseDto.builder().tenants(java.util.Collections.emptyList()).totalCount(0)
        .build();
  }
}
