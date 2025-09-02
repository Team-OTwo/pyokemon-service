package com.pyokemon.account.tenant.service;

import static com.pyokemon.account.remote.did.RegisterWalletRequest.AccountRole.TENANT;
import static com.pyokemon.common.exception.code.AccountErrorCodes.ACCOUNT_CREATION_FAILED;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pyokemon.account.auth.entity.Account;
import com.pyokemon.account.auth.service.AccountService;
import com.pyokemon.account.remote.did.RegisterWalletRequest;
import com.pyokemon.account.remote.did.RemoteDidService;
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
  private final RemoteDidService remoteDidService;
  private final AccountService accountService;

  @Transactional
  public TenantProfileResponseDto registerTenant(TenantRegisterRequestDto request) {

    try {
      accountService.existsByLoginId(request.getLoginId());
      existsByCorpId(request.getCorpId());

      Account account = accountService.registerAccount(request.toAccount());
      Tenant tenant = request.toTenant(account.getId());

      remoteDidService.registerWallet(RegisterWalletRequest.of(account.getId(), TENANT));
      tenantRepository.insert(tenant);

      return tenant.to(account.getLoginId());

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("테넌트 계정 생성 실패", ACCOUNT_CREATION_FAILED);
    }
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getTenantProfile(Long tenantId) {

    Tenant tenant = getTenantById(tenantId);
    Account account = accountService.getAccountById(tenant.getAccountId());

    return tenant.to(account.getLoginId());
  }

  @Transactional
  public TenantProfileResponseDto updateTenantProfile(Long tenantId,
      UpdateTenantProfileRequestDto request) {

    Tenant tenant = getTenantById(tenantId);
    Account account = accountService.getAccountById(tenant.getAccountId());

    Tenant updatedTenant = tenant.update(request);
    tenantRepository.update(updatedTenant);

    return updatedTenant.to(account.getLoginId());
  }

  @Transactional
  public void deleteTenant(Long tenantId) {

    Tenant tenant = getTenantById(tenantId);

    tenantRepository.delete(tenantId);
    accountService.deleteAccount(tenant.getAccountId());
  }

  @Transactional(readOnly = true)
  public TenantProfileResponseDto getMyTenantProfile(Long accountId) {

    Tenant tenant = getTenantByAccountId(accountId);
    Account account = accountService.getAccountById(tenant.getAccountId());

    return tenant.to(account.getLoginId());
  }

  @Transactional
  public TenantProfileResponseDto updateMyTenantProfile(Long accountId,
      UpdateTenantProfileRequestDto request) {

    Tenant tenant = getTenantByAccountId(accountId);
    Account account = accountService.getAccountById(tenant.getAccountId());

    Tenant updatedTenant = tenant.update(request);
    tenantRepository.update(updatedTenant);

    return updatedTenant.to(account.getLoginId());
  }

  @Transactional
  public void deleteMyTenantAccount(Long accountId) {

    Tenant tenant = getTenantByAccountId(accountId);

    tenantRepository.delete(tenant.getId());
    accountService.deleteAccount(tenant.getAccountId());
  }

  @Transactional(readOnly = true)
  public TenantListResponseDto getAllTenants() {
    List<Tenant> tenants = tenantRepository.findAll();

    List<TenantListResponseDto.TenantSummaryDto> tenantSummaries = tenants.stream().map(tenant -> {
      Account account = accountService.getAccountById(tenant.getAccountId());
      String loginId = account != null ? account.getLoginId() : null;
      return TenantListResponseDto.TenantSummaryDto.fromTenant(tenant, loginId);
    }).collect(Collectors.toList());

    return TenantListResponseDto.builder().tenants(tenantSummaries)
        .totalCount(tenantSummaries.size()).build();
  }


  private void existsByCorpId(String corpId) {
    if (tenantRepository.findByCorpId(corpId).isPresent()) {
      throw new BusinessException("이미 등록된 사업자번호입니다.", AccountErrorCodes.DUPLICATE_CORP_ID);
    }
  }

  private Tenant getTenantByAccountId(Long accountId) {
    return tenantRepository.findByAccountId(accountId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));
  }

  private Tenant getTenantById(Long tenantId) {
    return tenantRepository.findByTenantId(tenantId).orElseThrow(
        () -> new BusinessException("테넌트를 찾을 수 없습니다.", AccountErrorCodes.TENANT_NOT_FOUND));
  }

}
